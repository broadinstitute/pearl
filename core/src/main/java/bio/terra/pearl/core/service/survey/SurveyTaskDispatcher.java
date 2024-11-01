package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.RecurrenceType;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.event.SurveyPublishedEvent;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.TaskAssignable;
import bio.terra.pearl.core.service.workflow.TaskDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.*;

/** listens for events and updates enrollee survey tasks accordingly */
@Service
@Slf4j
public class SurveyTaskDispatcher extends TaskDispatcher<StudyEnvironmentSurvey> {
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final ParticipantTaskService participantTaskService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final EnrolleeContextService enrolleeContextService;
    private final SurveyService surveyService;


    public SurveyTaskDispatcher(StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                StudyEnvironmentService studyEnvironmentService, ParticipantTaskService participantTaskService,
                                EnrolleeService enrolleeService,
                                PortalParticipantUserService portalParticipantUserService,
                                EnrolleeContextService enrolleeContextService,
                                EnrolleeSearchExpressionParser enrolleeSearchExpressionParser, SurveyService surveyService) {
        super(studyEnvironmentService, participantTaskService, enrolleeService, enrolleeSearchExpressionParser);
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.participantTaskService = participantTaskService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.enrolleeContextService = enrolleeContextService;
        this.surveyService = surveyService;
    }

    @Override
    protected List<StudyEnvironmentSurvey> findByStudyEnvironment(UUID studyEnvId) {
        List<StudyEnvironmentSurvey> studyEnvironmentSurveys = studyEnvironmentSurveyService.findAllByStudyEnvId(studyEnvId, true);
        // load surveys
        studyEnvironmentSurveys
                .stream()
                .forEach(ses ->
                        ses.setSurvey(
                                surveyService
                                        .find(ses.getSurveyId())
                                        .orElseThrow(() -> new IllegalStateException("Could not find survey for StudyEnvironmentSurvey"))));
        return studyEnvironmentSurveys;

    }

    @Override
    protected StudyEnvironmentSurvey findByStableId(UUID studyEnvironmentId, String stableId) {
        Survey survey = surveyService
                .findByStudyEnvironmentIdAndStableIdNoContent(studyEnvironmentId, stableId)
                .orElseThrow(() -> new NotFoundException("Could not find survey"));

        StudyEnvironmentSurvey ses = studyEnvironmentSurveyService.findActiveBySurvey(studyEnvironmentId, survey.getId())
                .orElseThrow(() -> new NotFoundException("Could not find StudyEnvironmentSurvey"));
        ses.setSurvey(survey);

        return ses;
    }

    public List<ParticipantTask> assign(List<Enrollee> enrollees,
                                        StudyEnvironmentSurvey studyEnvironmentSurvey,
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
                        studyEnvironmentSurvey));
            } else {
                taskOpt = buildTaskIfApplicable(enrollees.get(i), existingTasks, ppUsers.get(i), enrolleeRuleData.get(i),
                        studyEnvironmentSurvey);
            }
            if (taskOpt.isPresent()) {
                ParticipantTask task = taskOpt.get();
                copyForwardResponseIfApplicable(task, studyEnvironmentSurvey.getSurvey(), existingTasks);
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

    @Override
    protected TaskType getTaskType(StudyEnvironmentSurvey ses) {
        return taskTypeForSurveyType.get(ses.getSurvey().getSurveyType());
    }

    /**
     * depending on recurrence type, will copy forward a past response so that updates are merged.
     * If we later support the 'prepopulate' option, this would be where would we clone the prior response
     * */
    protected void copyForwardResponseIfApplicable(ParticipantTask task, Survey survey, List<ParticipantTask> existingTasks) {
        if (survey.getRecurrenceType().equals(RecurrenceType.UPDATE)) {
            Optional<ParticipantTask> existingTask = existingTasks.stream()
                    .filter(t -> t.getTargetStableId().equals(task.getTargetStableId()))
                    .max(Comparator.comparing(ParticipantTask::getCreatedAt));
            existingTask.ifPresent(participantTask -> task.setSurveyResponseId(participantTask.getSurveyResponseId()));
        }
    }



    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void handleSurveyPublished(SurveyPublishedEvent event) {
        StudyEnvironmentSurvey studyEnvironmentSurvey = findByStableId(event.getStudyEnvironmentId(), event.getSurvey().getStableId());

        studyEnvironmentSurvey.setSurvey(event.getSurvey());

        handleNewAssignableTask(studyEnvironmentSurvey);
    }

    /** builds a task for the given survey -- does NOT evaluate the rule or check duplicates */
    @Override
    public ParticipantTask buildTask(Enrollee enrollee,
                                     PortalParticipantUser portalParticipantUser,
                                     StudyEnvironmentSurvey studyEnvSurvey) {
        if (!studyEnvSurvey.getSurveyId().equals(studyEnvSurvey.getSurvey().getId())) {
            throw new IllegalArgumentException("Survey does not match StudyEnvironmentSurvey");
        }
        Survey survey = studyEnvSurvey.getSurvey();
        TaskType taskType = taskTypeForSurveyType.get(survey.getSurveyType());
        ParticipantTask task = ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUser.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .blocksHub(survey.isRequired())
                .taskOrder(studyEnvSurvey.getSurveyOrder())
                .targetStableId(survey.getStableId())
                .targetAssignedVersion(survey.getVersion())
                .taskType(taskType)
                .targetName(survey.getName())
                .status(TaskStatus.NEW)
                .build();
        return task;
    }

    private final Map<SurveyType, TaskType> taskTypeForSurveyType = Map.of(
            SurveyType.CONSENT, TaskType.CONSENT,
            SurveyType.RESEARCH, TaskType.SURVEY,
            SurveyType.OUTREACH, TaskType.OUTREACH,
            SurveyType.ADMIN, TaskType.ADMIN_FORM
    );
}
