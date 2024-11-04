package bio.terra.pearl.core.model.document;

import bio.terra.pearl.core.model.workflow.RecurrenceType;
import bio.terra.pearl.core.service.workflow.TaskConfig;
import lombok.Getter;

import java.util.UUID;

@Getter
public class DocumentRequestTaskConfigDto implements TaskConfig {
    private final StudyEnvironmentDocumentRequest studyEnvironmentDocumentRequest;
    private final DocumentRequest documentRequest;

    public DocumentRequestTaskConfigDto(StudyEnvironmentDocumentRequest studyEnvironmentDocumentRequest, DocumentRequest documentRequest) {
        if (studyEnvironmentDocumentRequest == null || documentRequest == null) {
            throw new IllegalArgumentException("studyEnvironmentDocumentRequest and documentRequest must be non-null");
        }
        if (!studyEnvironmentDocumentRequest.getDocumentRequestId().equals(documentRequest.getId())) {
            throw new IllegalArgumentException("studyEnvironmentDocumentRequest and documentRequest must be for the same documentRequest");
        }

        this.studyEnvironmentDocumentRequest = studyEnvironmentDocumentRequest;
        this.documentRequest = documentRequest;
    }

    public DocumentRequestTaskConfigDto(StudyEnvironmentDocumentRequest studyEnvironmentDocumentRequest) {
        this(studyEnvironmentDocumentRequest, studyEnvironmentDocumentRequest.getDocumentRequest());
    }

    @Override
    public String getStableId() {
        return documentRequest.getStableId();
    }

    @Override
    public Integer getVersion() {
        return documentRequest.getVersion();
    }

    @Override
    public String getName() {
        return documentRequest.getDocumentName();
    }

    @Override
    public UUID getStudyEnvironmentId() {
        return studyEnvironmentDocumentRequest.getStudyEnvironmentId();
    }

    @Override
    public RecurrenceType getRecurrenceType() {
        return documentRequest.getRecurrenceType();
    }

    @Override
    public Integer getDaysAfterEligible() {
        return documentRequest.getDaysAfterEligible();
    }

    @Override
    public Integer getRecurrenceIntervalDays() {
        return documentRequest.getRecurrenceIntervalDays();
    }

    @Override
    public String getEligibilityRule() {
        return documentRequest.getEligibilityRule();
    }

    @Override
    public Boolean isAutoAssign() {
        return documentRequest.isAutoAssign();
    }

    @Override
    public Boolean isAutoUpdateTaskAssignments() {
        return documentRequest.isAutoUpdateTaskAssignments();
    }

    @Override
    public Boolean isAssignToExistingEnrollees() {
        return documentRequest.isAssignToExistingEnrollees();
    }

    @Override
    public Boolean isRequired() {
        return true;
    }

    @Override
    public Integer getTaskOrder() {
        return studyEnvironmentDocumentRequest.getTaskOrder();
    }

}
