package bio.terra.pearl.core.service.document;

import bio.terra.pearl.core.model.document.DocumentRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.survey.SurveyType;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.document.event.DocumentRequestPublished;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
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
public class DocumentRequestTaskDispatcher extends TaskConfigDispatcher<DocumentRequest, DocumentRequestPublished> {
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final SurveyService surveyService;


    public DocumentRequestTaskDispatcher(StudyEnvironmentSurveyService studyEnvironmentSurveyService,
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
    protected List<DocumentRequest> findTaskConfigsByStudyEnvironment(UUID studyEnvId) {
        // todo
        return List.of();
    }

    @Override
    protected DocumentRequest findTaskConfigByStableId(UUID studyEnvironmentId, String stableId, Integer version) {
        return null;
    }

    @Override
    protected TaskType getTaskType(DocumentRequest request) {
        return TaskType.DOCUMENT_REQUEST;
    }

    @Override
    protected void copyTaskData(ParticipantTask newTask, ParticipantTask oldTask, DocumentRequest documentRequest) {
        newTask.setParticipantFileId(oldTask.getParticipantFileId());
    }

    /** builds a task for the given survey -- does NOT evaluate the rule or check duplicates */
    @Override
    public ParticipantTask buildTask(Enrollee enrollee,
                                     PortalParticipantUser portalParticipantUser,
                                     DocumentRequest documentRequest) {
        ParticipantTask task = ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .portalParticipantUserId(portalParticipantUser.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .blocksHub(documentRequest.isRequired())
                .taskOrder(documentRequest.getTaskOrder())
                .targetStableId(documentRequest.getStableId())
                .targetAssignedVersion(documentRequest.getVersion())
                .taskType(getTaskType(documentRequest))
                .targetName(documentRequest.getDocumentName())
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
