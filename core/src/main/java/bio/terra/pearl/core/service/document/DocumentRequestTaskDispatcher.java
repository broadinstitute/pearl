package bio.terra.pearl.core.service.document;

import bio.terra.pearl.core.model.document.DocumentRequest;
import bio.terra.pearl.core.model.document.DocumentRequestTaskConfigDto;
import bio.terra.pearl.core.model.document.StudyEnvironmentDocumentRequest;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.document.event.DocumentRequestPublishedEvent;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.rule.EnrolleeContextService;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.event.EnrolleeSurveyEvent;
import bio.terra.pearl.core.service.workflow.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * listens for events and updates enrollee document request tasks accordingly
 */
@Service
@Slf4j
public class DocumentRequestTaskDispatcher extends TaskConfigDispatcher<DocumentRequestTaskConfigDto> {

    private final DocumentRequestService documentRequestService;
    private final StudyEnvironmentDocumentRequestService studyEnvironmentDocumentRequestService;

    public DocumentRequestTaskDispatcher(StudyEnvironmentService studyEnvironmentService,
                                         ParticipantTaskService participantTaskService,
                                         EnrolleeService enrolleeService,
                                         PortalParticipantUserService portalParticipantUserService,
                                         EnrolleeContextService enrolleeContextService,
                                         EnrolleeSearchExpressionParser enrolleeSearchExpressionParser,
                                         DocumentRequestService documentRequestService,
                                         StudyEnvironmentDocumentRequestService studyEnvironmentDocumentRequestService) {
        super(studyEnvironmentService, participantTaskService, enrolleeService, enrolleeSearchExpressionParser, enrolleeContextService, portalParticipantUserService);
        this.documentRequestService = documentRequestService;
        this.studyEnvironmentDocumentRequestService = studyEnvironmentDocumentRequestService;
    }

    @EventListener
    @Order(DispatcherOrder.DOCUMENT_REQUEST_TASK)
    public void handleNewTaskConfigEvent(DocumentRequestPublishedEvent newEvent) {
        DocumentRequestTaskConfigDto configDtoOpt = findByStableId(newEvent.getStudyEnvironmentId(), newEvent.getDocumentRequest().getStableId(), newEvent.getDocumentRequest().getVersion())
                .orElseThrow(() -> new IllegalStateException("Document request not found"));

        updateTasksForNewTaskConfig(configDtoOpt);
    }

    @EventListener
    @Order(DispatcherOrder.DOCUMENT_REQUEST_TASK)
    public void handleNewEnrolleeEvent(EnrolleeCreationEvent enrolleeEvent) {
        syncTasksForEnrollee(enrolleeEvent.getEnrolleeContext());
    }

    @EventListener
    @Order(DispatcherOrder.DOCUMENT_REQUEST_TASK)
    public void handleSurveyEvent(EnrolleeSurveyEvent enrolleeEvent) {
        syncTasksForEnrollee(enrolleeEvent.getEnrolleeContext());
    }


    @Override
    protected List<DocumentRequestTaskConfigDto> findTaskConfigsByStudyEnvironment(UUID studyEnvId) {
        List<StudyEnvironmentDocumentRequest> studyEnvironmentDocumentRequests = studyEnvironmentDocumentRequestService.findActiveByStudyEnvironmentId(studyEnvId);

        return studyEnvironmentDocumentRequests
                .stream()
                .map(studyEnvironmentDocumentRequest -> {
                    Optional<DocumentRequest> documentRequest = documentRequestService.find(studyEnvironmentDocumentRequest.getDocumentRequestId());
                    if (documentRequest.isEmpty()) {
                        log.error("Document request not found for study environment document request id: {}", studyEnvironmentDocumentRequest.getId());
                        return null;
                    }

                    return new DocumentRequestTaskConfigDto(studyEnvironmentDocumentRequest, documentRequest.get());
                }).toList();
    }

    @Override
    protected Optional<DocumentRequestTaskConfigDto> findTaskConfigForAssignDto(UUID studyEnvironmentId, ParticipantTaskAssignDto participantTaskAssignDto) {
        return findByStableId(studyEnvironmentId, participantTaskAssignDto.targetStableId(), participantTaskAssignDto.targetAssignedVersion());
    }

    private Optional<DocumentRequestTaskConfigDto> findByStableId(UUID studyEnvironmentId, String stableId, Integer version) {
        Optional<DocumentRequest> documentRequest = documentRequestService.findActiveInStudyEnvByStableId(stableId, version);
        if (documentRequest.isEmpty()) {
            return Optional.empty();
        }

        Optional<StudyEnvironmentDocumentRequest> studyEnvironmentDocumentRequest = studyEnvironmentDocumentRequestService.findByDocumentRequestId(studyEnvironmentId, documentRequest.get().getId());
        if (studyEnvironmentDocumentRequest.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new DocumentRequestTaskConfigDto(studyEnvironmentDocumentRequest.get(), documentRequest.get()));
    }

    @Override
    protected TaskType getTaskType(DocumentRequestTaskConfigDto request) {
        return TaskType.DOCUMENT_REQUEST;
    }

}
