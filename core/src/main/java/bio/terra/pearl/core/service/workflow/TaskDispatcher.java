package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.RecurrenceType;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** listens for events and updates enrollee survey tasks accordingly */
@Service
@Slf4j
public abstract class TaskDispatcher<T extends TaskAssignable> {
    private final StudyEnvironmentService studyEnvironmentService;
    private final ParticipantTaskService participantTaskService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;


    public TaskDispatcher(StudyEnvironmentService studyEnvironmentService,
                          ParticipantTaskService participantTaskService,
                          EnrolleeService enrolleeService,
                          EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
        this.studyEnvironmentService = studyEnvironmentService;
        this.participantTaskService = participantTaskService;
        this.enrolleeService = enrolleeService;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
    }

    // todo: call this from cron job
    public void assignScheduledTasks() {
        List<StudyEnvironment> studyEnvironments = studyEnvironmentService.findAll();
        for (StudyEnvironment studyEnv : studyEnvironments) {
            assignScheduledTasks(studyEnv);
        }
    }

    protected abstract List<T> findByStudyEnvironment(UUID studyEnvId);

    public void assignScheduledTasks(StudyEnvironment studyEnv) {
        List<T> assignableTasks = findByStudyEnvironment(studyEnv.getId());
        for (T assignableTask : assignableTasks) {
            if (assignableTask.getRecurrenceType() != RecurrenceType.NONE && assignableTask.getRecurrenceIntervalDays() != null) {
                assignRecurring(assignableTask);
            }
            if (assignableTask.getDaysAfterEligible() != null && assignableTask.getDaysAfterEligible() > 0) {
                assignDelayedSurvey(assignableTask);
            }
        }
    }

    /** will assign a recurringsurvey to enrollees who have already taken it at least once, but are due to take it again */
    public void assignRecurring(T assignableTask) {
        List<Enrollee> enrollees = enrolleeService.findWithTaskInPast(
                assignableTask.getStudyEnvironmentId(),
                assignableTask.getStableId(),
                Duration.of(assignableTask.getRecurrenceIntervalDays(), ChronoUnit.DAYS));
        assign(enrollees, assignableTask, false, new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "assignRecurringSurvey")));
    }

    /** will assign a delayed survey to enrollees who have never taken it, but are due to take it now */
    public void assignDelayedSurvey(T assignableTask) {
        List<Enrollee> enrollees = enrolleeService.findUnassignedToTask(assignableTask.getStudyEnvironmentId(), assignableTask.getStableId(), null);
        enrollees = enrollees.stream().filter(enrollee ->
                enrollee.getCreatedAt().plus(assignableTask.getDaysAfterEligible(), ChronoUnit.DAYS)
                        .isBefore(Instant.now())).toList();
        assign(enrollees, assignableTask, false, new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "assignDelayedSurvey")));
    }

    protected abstract T findByStableId(UUID studyEnvironmentId, String stableId);

    public List<ParticipantTask> assign(ParticipantTaskAssignDto assignDto,
                                        UUID studyEnvironmentId,
                                        ResponsibleEntity operator) {
        List<Enrollee> enrollees = findMatchingEnrollees(assignDto, studyEnvironmentId);
        T assignable = findByStableId(studyEnvironmentId, assignDto.targetStableId());
        return assign(enrollees, assignable, assignDto.overrideEligibility(), operator);
    }

    public abstract List<ParticipantTask> assign(List<Enrollee> enrollees,
                                        T studyEnvironmentSurvey,
                                        boolean overrideEligibility,
                                        ResponsibleEntity operator);

    protected List<Enrollee> findMatchingEnrollees(ParticipantTaskAssignDto assignDto,
                                                   UUID studyEnvironmentId) {
        if (assignDto.assignAllUnassigned()
                && (Objects.isNull(assignDto.enrolleeIds()) || assignDto.enrolleeIds().isEmpty())) {
            return enrolleeService.findUnassignedToTask(studyEnvironmentId,
                    assignDto.targetStableId(), null);
        } else {
            return enrolleeService.findAll(assignDto.enrolleeIds());
        }
    }

    /** create the survey tasks for an enrollee's initial creation */
    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void updateTasksForNewEnrollee(EnrolleeCreationEvent enrolleeEvent) {
        updateTasksFromEnrolleeEvent(enrolleeEvent);
    }

    /** survey responses can update what surveys a person is eligible for -- recompute as needed */
    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void updateTasksFromSurvey(EnrolleeSurveyEvent enrolleeEvent) {
        /** for now, only recompute on updates involving a completed survey.  This will
         * avoid assigning surveys based on an answer that was quickly changed, since we don't
         * yet have functions for unassigning surveys */
        if (!enrolleeEvent.getSurveyResponse().isComplete()) {
            return;
        }
        updateTasksFromEnrolleeEvent(enrolleeEvent);
    }

    protected void updateTasksFromEnrolleeEvent(EnrolleeEvent enrolleeEvent) {
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .systemProcess(getClass().getSimpleName() + ".updateSurveyTasks")
                .portalParticipantUserId(enrolleeEvent.getPortalParticipantUser().getId())
                .enrolleeId(enrolleeEvent.getEnrollee().getId()).build();

        List<T> taskAssignables = findByStudyEnvironment(enrolleeEvent.getEnrolleeContext().getEnrollee().getStudyEnvironmentId());

        for (T taskAssignable: taskAssignables) {
            if (taskAssignable.isAutoAssign()) {
                createTaskIfApplicable(taskAssignable, enrolleeEvent, auditInfo);
            }
        }
    }


    private void createTaskIfApplicable(T taskAssignable, EnrolleeEvent event, DataAuditInfo auditInfo) {
        Optional<ParticipantTask> taskOpt = buildTaskIfApplicable(event.getEnrollee(),
                event.getEnrollee().getParticipantTasks(),
                event.getPortalParticipantUser(), event.getEnrolleeContext(),
                taskAssignable);
        if (taskOpt.isPresent()) {
            ParticipantTask task = taskOpt.get();
            log.info("Task creation: enrollee {}  -- task {}, target {}", event.getEnrollee().getShortcode(),
                    task.getTaskType(), task.getTargetStableId());
            task = participantTaskService.create(task, auditInfo);
            event.getEnrollee().getParticipantTasks().add(task);
        }
    }

    // todo listen for survey published events and update tasks accordingly
    public void handleNewAssignableTask(T newAssignableTask) {
        if (newAssignableTask.isAutoUpdateTaskAssignments()) {
            ParticipantTaskUpdateDto updateDto = new ParticipantTaskUpdateDto(
                    List.of(new ParticipantTaskUpdateDto.TaskUpdateSpec(
                            newAssignableTask.getStableId(),
                            newAssignableTask.getVersion(),
                            null,
                            null)),
                    null,
                    true
                    );
            participantTaskService.updateTasks(
                    newAssignableTask.getStudyEnvironmentId(),
                    updateDto,
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleSurveyPublished.autoUpdateTaskAssignments"))
            );
        }
        if (newAssignableTask.isAssignToExistingEnrollees()) {
            ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(
                    getTaskType(newAssignableTask),
                    newAssignableTask.getStableId(),
                    newAssignableTask.getVersion(),
                null,
                    true,
                    false);

            assign(assignDto, newAssignableTask.getStudyEnvironmentId(),
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleNewAssignableTask.assignToExistingEnrollees")));

        }
    }

    protected abstract  TaskType getTaskType(T taskAssignable);


    /** builds any survey tasks that the enrollee is eligible for that are not duplicates
     *  Does not add them to the event or persist them.
     *  */
    public Optional<ParticipantTask> buildTaskIfApplicable(Enrollee enrollee,
                                                      List<ParticipantTask> existingEnrolleeTasks,
                                                      PortalParticipantUser portalParticipantUser,
                                                      EnrolleeContext enrolleeContext,
                                                      T taskAssignable) {
        if (isEligible(taskAssignable, enrolleeContext)) {
            ParticipantTask task = buildTask(enrollee, portalParticipantUser, taskAssignable);
            if (!isDuplicateTask(taskAssignable, task, existingEnrolleeTasks)) {
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }

    public boolean isEligible(T taskAssignable, EnrolleeContext enrolleeContext) {
        /**
         * eligible if the enrollee is a subject, the survey is not restricted by time, and the enrollee meets the rule
         * note that this does not include a duplicate task check -- that is done elsewhere
         */
        // TODO JN-977: this logic will need to change because we will need to support surveys for proxies
        return enrolleeContext.getEnrollee().isSubject() &&
                (taskAssignable.getDaysAfterEligible() == null ||
                        enrolleeContext.getEnrollee().getCreatedAt().plus(taskAssignable.getDaysAfterEligible(), ChronoUnit.DAYS).isBefore(Instant.now())) &&
                enrolleeSearchExpressionParser
                .parseRule(taskAssignable.getEligibilityRule())
                .evaluate(new EnrolleeSearchContext(enrolleeContext.getEnrollee(), enrolleeContext.getProfile()));
    }

    /** builds a task for the given survey -- does NOT evaluate the rule or check duplicates */
    public abstract ParticipantTask buildTask(Enrollee enrollee, PortalParticipantUser portalParticipantUser, T taskAssignable);

    /**
     * To avoid accidentally assigning the same survey or outreach activity to a participant multiple times,
     * confirm that if the stableId matches an existing task, the existing task must be complete and the
     * configured survey must allow recurrence.
     */
    public boolean isDuplicateTask(T taskAssignable, ParticipantTask task,
                                   List<ParticipantTask> allTasks) {
        return !allTasks.stream()
                .filter(existingTask ->
                        existingTask.getTaskType().equals(task.getTaskType()) &&
                        existingTask.getTargetStableId().equals(task.getTargetStableId()) &&
                        !isRecurrenceWindowOpen(taskAssignable, existingTask))
                .toList().isEmpty();
    }

    /**
     * whether or not sufficient time has passed since a previous instance of a survey being assigned to assign
     * a new one
     */
    public boolean isRecurrenceWindowOpen(T taskAssignable, ParticipantTask pastTask) {
        if (taskAssignable.getRecurrenceType() == RecurrenceType.NONE) {
            return false;
        }
        Instant pastCutoffTime = ZonedDateTime.now(ZoneOffset.UTC)
                .minusDays(taskAssignable.getRecurrenceIntervalDays()).toInstant();
        return pastTask.getCreatedAt().isBefore(pastCutoffTime);
    }

    public abstract ParticipantTask buildTask(Enrollee enrollee,
                                              PortalParticipantUser portalParticipantUser,
                                              StudyEnvironmentSurvey studyEnvSurvey);
}
