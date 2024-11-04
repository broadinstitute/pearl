package bio.terra.pearl.core.service.document;

import bio.terra.pearl.core.model.document.DocumentRequest;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.document.event.DocumentRequestPublished;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.workflow.DispatcherOrder;
import bio.terra.pearl.core.service.workflow.EnrolleeCreationEvent;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.TaskConfigDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/** listens for events and updates enrollee survey tasks accordingly */
@Service
@Slf4j
public class DocumentRequestTaskDispatcher extends TaskConfigDispatcher<DocumentRequest, DocumentRequestPublished> {


    public DocumentRequestTaskDispatcher(StudyEnvironmentService studyEnvironmentService,
                                         ParticipantTaskService participantTaskService,
                                         EnrolleeService enrolleeService,
                                         PortalParticipantUserService portalParticipantUserService,
                                         EnrolleeContextService enrolleeContextService,
                                         EnrolleeSearchExpressionParser enrolleeSearchExpressionParser) {
        super(studyEnvironmentService, participantTaskService, enrolleeService, enrolleeSearchExpressionParser, enrolleeContextService, portalParticipantUserService);


    }

    @EventListener
    @Order(DispatcherOrder.SURVEY_TASK)
    public void handleNewTaskConfigEvent(DocumentRequestPublished newEvent) {
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
}
