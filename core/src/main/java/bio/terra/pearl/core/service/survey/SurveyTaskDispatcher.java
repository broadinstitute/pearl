package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyTaskConfigDto;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.survey.event.SurveyPublishedEvent;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeCreationEvent;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.TaskConfigDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** listens for events and updates enrollee survey tasks accordingly */
@Service
@Slf4j
public class SurveyTaskDispatcher extends TaskConfigDispatcher<SurveyTaskConfigDto, SurveyPublishedEvent> {
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

    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void handleNewTaskConfigEvent(SurveyPublishedEvent newEvent) {
        super.handleNewTaskConfigEvent(newEvent);
    }

    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void handleNewEnrolleeEvent(EnrolleeCreationEvent enrolleeEvent) {
        super.handleNewEnrolleeEvent(enrolleeEvent);
    }

    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void handleSurveyEvent(EnrolleeSurveyEvent enrolleeEvent) {
        super.handleSurveyEvent(enrolleeEvent);
    }


    @Override
    protected List<SurveyTaskConfigDto> findTaskConfigsByStudyEnvironment(UUID studyEnvId) {
        List<StudyEnvironmentSurvey> studyEnvironmentSurveys = studyEnvironmentSurveyService.findAllByStudyEnvId(studyEnvId, true);
        // load surveys
        return studyEnvironmentSurveys.stream()
                .map(ses -> {
                    Survey survey = surveyService.find(ses.getSurveyId())
                            .orElseThrow(() -> new NotFoundException("Could not find survey"));
                    return new SurveyTaskConfigDto(ses, survey);
                })
                .toList();

    }

    @Override
    protected SurveyTaskConfigDto findTaskConfigByStableId(UUID studyEnvironmentId, String stableId, Integer version) {
        Survey survey = surveyService
                .findActiveByStudyEnvironmentIdAndStableIdNoContent(studyEnvironmentId, stableId, version)
                .orElseThrow(() -> new NotFoundException("Could not find survey"));

        StudyEnvironmentSurvey ses = studyEnvironmentSurveyService.findActiveBySurvey(studyEnvironmentId, survey.getId())
                .orElseThrow(() -> new NotFoundException("Could not find StudyEnvironmentSurvey"));

        return new SurveyTaskConfigDto(ses, survey);
    }

    @Override
    protected void copyTaskData(ParticipantTask newTask, ParticipantTask oldTask, SurveyTaskConfigDto taskDto) {
        newTask.setSurveyResponseId(oldTask.getSurveyResponseId());
    }


    @Override
    protected TaskType getTaskType(SurveyTaskConfigDto taskDto) {
        return taskTypeForSurveyType.get(taskDto.getSurvey().getSurveyType());
    }

    private final Map<SurveyType, TaskType> taskTypeForSurveyType = Map.of(
            SurveyType.CONSENT, TaskType.CONSENT,
            SurveyType.RESEARCH, TaskType.SURVEY,
            SurveyType.OUTREACH, TaskType.OUTREACH,
            SurveyType.ADMIN, TaskType.ADMIN_FORM
    );
}
