package bio.terra.pearl.core.factory.document;

import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.document.DocumentRequest;
import bio.terra.pearl.core.model.document.StudyEnvironmentDocumentRequest;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.document.DocumentRequestService;
import bio.terra.pearl.core.service.document.StudyEnvironmentDocumentRequestService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DocumentRequestFactory {
    @Autowired
    private DocumentRequestService documentRequestService;

    @Autowired
    private PortalFactory portalFactory;

    @Autowired
    private StudyEnvironmentDocumentRequestService studyEnvironmentDocumentRequestService;

    public DocumentRequest.DocumentRequestBuilder builder(String testName) {
        return DocumentRequest
                .builder()
                .stableId(testName + "_" + RandomStringUtils.randomAlphabetic(5))
                .documentName(testName)
                .allowedFileTypes("csv")
                .blurb("blurb")
                .autoAssign(true)
                .assignToExistingEnrollees(true)
                .autoUpdateTaskAssignments(true)
                .multipleFilesAllowed(false)
                .eligibilityRule("");
    }

    public DocumentRequest.DocumentRequestBuilder builderWithDependencies(String testName) {
        Portal portal = portalFactory.buildPersisted(testName);

        return builder(testName)
                .portalId(portal.getId());
    }

    public DocumentRequest buildPersisted(String testName) {
        DocumentRequest documentRequest = builderWithDependencies(testName).build();
        return documentRequestService.create(documentRequest);
    }

    public DocumentRequest buildPersisted(String testName, UUID portalId) {
        DocumentRequest documentRequest = builder(testName)
                .portalId(portalId)
                .build();
        return documentRequestService.create(documentRequest);
    }

    public StudyEnvironmentDocumentRequest attachToStudyEnvironment(String testName, DocumentRequest documentRequest, UUID studyEnvId) {
        return studyEnvironmentDocumentRequestService.create(StudyEnvironmentDocumentRequest.builder()
                .documentRequestId(documentRequest.getId())
                .studyEnvironmentId(studyEnvId)
                .active(true)
                .taskOrder(0)
                .build());
    }
}
