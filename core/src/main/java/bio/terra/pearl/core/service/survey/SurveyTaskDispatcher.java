package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.event.SurveyPublishedEvent;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.TaskConfigDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** listens for events and updates enrollee survey tasks accordingly */
@Service
@Slf4j
public class SurveyTaskDispatcher extends TaskConfigDispatcher<StudyEnvironmentSurvey, SurveyPublishedEvent> {
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final SurveyService surveyService;


    public SurveyTaskDispatcher(StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                                StudyEnvironmentService studyEnvironmentService, ParticipantTaskService participantTaskService,
                                EnrolleeService enrolleeService,
                                PortalParticipantUserService portalParticipantUserService,
                                EnrolleeContextService enrolleeContextService,
                                EnrolleeSearchExpressionParser enrolleeSearchExpressionParser, SurveyService surveyService) {
        super(studyEnvironmentService, participantTaskService, enrolleeService, enrolleeSearchExpressionParser, enrolleeContextService, portalParticipantUserService);
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.surveyService = surveyService;
    }


    @Override
    protected List<StudyEnvironmentSurvey> findTaskConfigsByStudyEnvironment(UUID studyEnvId) {
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
    protected StudyEnvironmentSurvey findTaskConfigByStableId(UUID studyEnvironmentId, String stableId, Integer version) {
        System.out.println("stableId: " + stableId);
        System.out.println("version: " + version);
        System.out.println("studyEnvironmentId: " + studyEnvironmentId);
        Survey survey = surveyService
                .findActiveByStudyEnvironmentIdAndStableIdNoContent(studyEnvironmentId, stableId, version)
                .orElseThrow(() -> new NotFoundException("Could not find survey"));

        StudyEnvironmentSurvey ses = studyEnvironmentSurveyService.findActiveBySurvey(studyEnvironmentId, survey.getId())
                .orElseThrow(() -> new NotFoundException("Could not find StudyEnvironmentSurvey"));
        ses.setSurvey(survey);

        return ses;
    }

    @Override
    protected TaskType getTaskType(StudyEnvironmentSurvey ses) {
        return taskTypeForSurveyType.get(ses.getSurvey().getSurveyType());
    }

    @Override
    protected void copyTaskData(ParticipantTask newTask, ParticipantTask oldTask, StudyEnvironmentSurvey ses) {
        newTask.setSurveyResponseId(oldTask.getSurveyResponseId());
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
