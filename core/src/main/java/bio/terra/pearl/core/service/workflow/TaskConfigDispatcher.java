package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.RecurrenceType;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Handles dispatching tasks to enrollees based on task configurations.
 */
@Service
@Slf4j
public abstract class TaskConfigDispatcher<T extends TaskConfig, E extends TaskConfigCreatedEvent> {
    private final StudyEnvironmentService studyEnvironmentService;
    private final ParticipantTaskService participantTaskService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;
    private final EnrolleeContextService enrolleeContextService;
    private final PortalParticipantUserService portalParticipantUserService;


    public TaskConfigDispatcher(StudyEnvironmentService studyEnvironmentService,
                                ParticipantTaskService participantTaskService,
                                EnrolleeService enrolleeService,
                                EnrolleeSearchExpressionParser enrolleeSearchExpressionParser,
                                EnrolleeContextService enrolleeContextService,
                                PortalParticipantUserService portalParticipantUserService) {
        this.studyEnvironmentService = studyEnvironmentService;
        this.participantTaskService = participantTaskService;
        this.enrolleeService = enrolleeService;
        this.enrolleeSearchExpressionParser = enrolleeSearchExpressionParser;
        this.enrolleeContextService = enrolleeContextService;
        this.portalParticipantUserService = portalParticipantUserService;
    }

    /**
     * handle new task config creation/publishing
     */
    public void handleNewTaskConfigEvent(E newEvent) {
        T newTaskConfig = findTaskConfigByStableId(newEvent.getStudyEnvironmentId(), newEvent.getStableId(), newEvent.getVersion());
        handleNewTaskConfig(newTaskConfig);
    }

    /**
     * create the survey tasks for an enrollee's initial creation
     */
    public void handleNewEnrolleeEvent(EnrolleeCreationEvent enrolleeEvent) {
        updateTasksFromEnrolleeEvent(enrolleeEvent);
    }

    /**
     * survey responses can update what surveys a person is eligible for -- recompute as needed
     */
    public void handleSurveyEvent(EnrolleeSurveyEvent enrolleeEvent) {
        /** for now, only recompute on updates involving a completed survey.  This will
         * avoid assigning surveys based on an answer that was quickly changed, since we don't
         * yet have functions for unassigning surveys */
        if (!enrolleeEvent.getSurveyResponse().isComplete()) {
            return;
        }
        updateTasksFromEnrolleeEvent(enrolleeEvent);
    }

    protected abstract List<T> findTaskConfigsByStudyEnvironment(UUID studyEnvId);

    protected abstract T findTaskConfigByStableId(UUID studyEnvironmentId, String stableId, Integer version);

    protected abstract void copyTaskData(ParticipantTask newTask, ParticipantTask oldTask, T taskDispatchConfig);

    protected abstract TaskType getTaskType(T taskDispatchConfig);

    public void assignScheduledTasks() {
        List<StudyEnvironment> studyEnvironments = studyEnvironmentService.findAll();
        for (StudyEnvironment studyEnv : studyEnvironments) {
            assignScheduledTasks(studyEnv);
        }
    }

    public void assignScheduledTasks(StudyEnvironment studyEnv) {
        List<T> assignableTasks = findTaskConfigsByStudyEnvironment(studyEnv.getId());
        for (T assignableTask : assignableTasks) {
            if (assignableTask.getRecurrenceType() != RecurrenceType.NONE && assignableTask.getRecurrenceIntervalDays() != null) {
                assignRecurring(assignableTask);
            }
            if (assignableTask.getDaysAfterEligible() != null && assignableTask.getDaysAfterEligible() > 0) {
                assignDelayedSurvey(assignableTask);
            }
        }
    }

    /**
     * will assign a recurring task to enrollees who have already taken it at least once, but are due to take it again
     */
    public void assignRecurring(T taskConfig) {
        List<Enrollee> enrollees = enrolleeService.findWithTaskInPast(
                taskConfig.getStudyEnvironmentId(),
                taskConfig.getStableId(),
                Duration.of(taskConfig.getRecurrenceIntervalDays(), ChronoUnit.DAYS));
        assign(enrollees, taskConfig, false, new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "assignRecurringSurvey")));
    }

    /**
     * will assign a delayed task to enrollees who have never taken it, but are due to take it now
     */
    public void assignDelayedSurvey(T taskConfig) {
        List<Enrollee> enrollees = enrolleeService.findUnassignedToTask(taskConfig.getStudyEnvironmentId(), taskConfig.getStableId(), null);
        enrollees = enrollees.stream().filter(enrollee ->
                enrollee.getCreatedAt().plus(taskConfig.getDaysAfterEligible(), ChronoUnit.DAYS)
                        .isBefore(Instant.now())).toList();
        assign(enrollees, taskConfig, false, new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "assignDelayedSurvey")));
    }

    public List<ParticipantTask> assign(ParticipantTaskAssignDto assignDto,
                                        UUID studyEnvironmentId,
                                        ResponsibleEntity operator) {
        List<Enrollee> enrollees = findMatchingEnrollees(assignDto, studyEnvironmentId);
        T taskDispatchConfig = findTaskConfigByStableId(studyEnvironmentId, assignDto.targetStableId(), assignDto.targetAssignedVersion());
        return assign(enrollees, taskDispatchConfig, assignDto.overrideEligibility(), operator);
    }

    public List<ParticipantTask> assign(List<Enrollee> enrollees,
                                        T taskDispatchConfig,
                                        boolean overrideEligibility,
                                        ResponsibleEntity operator) {
        List<UUID> profileIds = enrollees.stream().map(Enrollee::getProfileId).toList();
        List<PortalParticipantUser> ppUsers = portalParticipantUserService.findByProfileIds(profileIds);
        if (ppUsers.size() != enrollees.size()) {
            throw new IllegalStateException("Task dispatch failed: Portal participant user not matched to enrollee");
        }
        List<EnrolleeContext> enrolleeRuleData = enrolleeContextService.fetchData(enrollees.stream().map(Enrollee::getId).toList());

        UUID auditOperationId = UUID.randomUUID();
        List<ParticipantTask> createdTasks = new ArrayList<>();
        for (int i = 0; i < enrollees.size(); i++) {
            List<ParticipantTask> existingTasks = participantTaskService.findByEnrolleeId(enrollees.get(i).getId());
            Optional<ParticipantTask> taskOpt;
            if (overrideEligibility) {
                taskOpt = Optional.of(buildTask(enrollees.get(i), ppUsers.get(i),
                        taskDispatchConfig));
            } else {
                taskOpt = buildTaskIfApplicable(enrollees.get(i), existingTasks, ppUsers.get(i), enrolleeRuleData.get(i),
                        taskDispatchConfig);
            }
            if (taskOpt.isPresent()) {
                ParticipantTask task = taskOpt.get();
                copyForwardDataIfApplicable(task, taskDispatchConfig, existingTasks);
                DataAuditInfo auditInfo = DataAuditInfo.builder()
                        .portalParticipantUserId(ppUsers.get(i).getId())
                        .operationId(auditOperationId)
                        .enrolleeId(enrollees.get(i).getId()).build();
                auditInfo.setResponsibleEntity(operator);

                task = participantTaskService.create(task, auditInfo);
                log.info("Task creation: enrollee {}  -- task {}, target {}", enrollees.get(i).getShortcode(),
                        task.getTaskType(), task.getTargetStableId());
                createdTasks.add(task);
            }
        }
        return createdTasks;
    }

    protected void copyForwardDataIfApplicable(ParticipantTask task, T taskDispatchConfig, List<ParticipantTask> existingTasks) {
        if (taskDispatchConfig.getRecurrenceType().equals(RecurrenceType.UPDATE)) {
            Optional<ParticipantTask> existingTask = existingTasks.stream()
                    .filter(t -> t.getTargetStableId().equals(task.getTargetStableId()))
                    .max(Comparator.comparing(ParticipantTask::getCreatedAt));
            existingTask.ifPresent(participantTask -> copyTaskData(task, participantTask, taskDispatchConfig));
        }
    }

    protected void updateTasksFromEnrolleeEvent(EnrolleeEvent enrolleeEvent) {
        DataAuditInfo auditInfo = DataAuditInfo.builder()
                .systemProcess(getClass().getSimpleName() + ".updateSurveyTasks")
                .portalParticipantUserId(enrolleeEvent.getPortalParticipantUser().getId())
                .enrolleeId(enrolleeEvent.getEnrollee().getId()).build();

        List<T> taskAssignables = findTaskConfigsByStudyEnvironment(enrolleeEvent.getEnrolleeContext().getEnrollee().getStudyEnvironmentId());

        for (T taskDispatchConfig : taskAssignables) {
            if (taskDispatchConfig.isAutoAssign()) {
                createTaskIfApplicable(taskDispatchConfig, enrolleeEvent, auditInfo);
            }
        }
    }


    private void createTaskIfApplicable(T taskDispatchConfig, EnrolleeEvent event, DataAuditInfo auditInfo) {
        Optional<ParticipantTask> taskOpt = buildTaskIfApplicable(event.getEnrollee(),
                event.getEnrollee().getParticipantTasks(),
                event.getPortalParticipantUser(), event.getEnrolleeContext(),
                taskDispatchConfig);
        if (taskOpt.isPresent()) {
            ParticipantTask task = taskOpt.get();
            log.info("Task creation: enrollee {}  -- task {}, target {}", event.getEnrollee().getShortcode(),
                    task.getTaskType(), task.getTargetStableId());
            task = participantTaskService.create(task, auditInfo);
            event.getEnrollee().getParticipantTasks().add(task);
        }
    }

    public void handleNewTaskConfig(T newTaskConfig) {
        if (newTaskConfig.isAutoUpdateTaskAssignments()) {
            ParticipantTaskUpdateDto updateDto = new ParticipantTaskUpdateDto(
                    List.of(new ParticipantTaskUpdateDto.TaskUpdateSpec(
                            newTaskConfig.getStableId(),
                            newTaskConfig.getVersion(),
                            null,
                            null)),
                    null,
                    true
                    );
            participantTaskService.updateTasks(
                    newTaskConfig.getStudyEnvironmentId(),
                    updateDto,
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleSurveyPublished.autoUpdateTaskAssignments"))
            );
        }
        if (newTaskConfig.isAssignToExistingEnrollees()) {
            ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(
                    getTaskType(newTaskConfig),
                    newTaskConfig.getStableId(),
                    newTaskConfig.getVersion(),
                null,
                    true,
                    false);

            assign(assignDto, newTaskConfig.getStudyEnvironmentId(),
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleNewAssignableTask.assignToExistingEnrollees")));

        }
    }

    /** builds any tasks that the enrollee is eligible for that are not duplicates
     *  Does not add them to the event or persist them.
     *  */
    public Optional<ParticipantTask> buildTaskIfApplicable(Enrollee enrollee,
                                                      List<ParticipantTask> existingEnrolleeTasks,
                                                      PortalParticipantUser portalParticipantUser,
                                                      EnrolleeContext enrolleeContext,
                                                           T taskDispatchConfig) {
        if (isEligible(taskDispatchConfig, enrolleeContext)) {
            ParticipantTask task = buildTask(enrollee, portalParticipantUser, taskDispatchConfig);
            if (!isDuplicateTask(taskDispatchConfig, task, existingEnrolleeTasks)) {
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }

    /**
     * builds a task for the given survey -- does NOT evaluate the rule or check duplicates
     */
    public ParticipantTask buildTask(Enrollee enrollee,
                                     PortalParticipantUser portalParticipantUser,
                                     T taskConfig) {
        TaskType taskType = getTaskType(taskConfig);
        ParticipantTask task = ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUser.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .blocksHub(taskConfig.isRequired())
                .taskOrder(taskConfig.getTaskOrder())
                .targetStableId(taskConfig.getStableId())
                .targetAssignedVersion(taskConfig.getVersion())
                .taskType(taskType)
                .targetName(taskConfig.getName())
                .status(TaskStatus.NEW)
                .build();
        return task;
    }

    public boolean isEligible(T taskDispatchConfig, EnrolleeContext enrolleeContext) {
        /**
         * eligible if the enrollee is a subject, the task is not restricted by time, and the enrollee meets the rule
         * note that this does not include a duplicate task check -- that is done elsewhere
         */
        // TODO JN-977: this logic will need to change because we will need to support surveys for proxies
        return enrolleeContext.getEnrollee().isSubject() &&
                (taskDispatchConfig.getDaysAfterEligible() == null ||
                        enrolleeContext.getEnrollee().getCreatedAt().plus(taskDispatchConfig.getDaysAfterEligible(), ChronoUnit.DAYS).isBefore(Instant.now())) &&
                enrolleeSearchExpressionParser
                        .parseRule(taskDispatchConfig.getEligibilityRule())
                        .evaluate(new EnrolleeSearchContext(enrolleeContext.getEnrollee(), enrolleeContext.getProfile()));
    }


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

    /**
     * To avoid accidentally assigning the same tasks or outreach activity to a participant multiple times,
     * confirm that if the stableId matches an existing task, the existing task must be complete and the
     * configured survey must allow recurrence.
     */
    public boolean isDuplicateTask(T taskDispatchConfig, ParticipantTask task,
                                   List<ParticipantTask> allTasks) {
        return !allTasks.stream()
                .filter(existingTask ->
                        existingTask.getTaskType().equals(task.getTaskType()) &&
                        existingTask.getTargetStableId().equals(task.getTargetStableId()) &&
                                !isRecurrenceWindowOpen(taskDispatchConfig, existingTask))
                .toList().isEmpty();
    }

    /**
     * whether or not sufficient time has passed since a previous instance of a task being assigned to assign
     * a new one
     */
    public boolean isRecurrenceWindowOpen(T taskDispatchConfig, ParticipantTask pastTask) {
        if (taskDispatchConfig.getRecurrenceType() == RecurrenceType.NONE) {
            return false;
        }
        Instant pastCutoffTime = ZonedDateTime.now(ZoneOffset.UTC)
                .minusDays(taskDispatchConfig.getRecurrenceIntervalDays()).toInstant();
        return pastTask.getCreatedAt().isBefore(pastCutoffTime);
    }
}