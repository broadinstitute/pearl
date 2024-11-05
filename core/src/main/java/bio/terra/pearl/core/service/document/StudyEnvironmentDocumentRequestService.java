package bio.terra.pearl.core.service.document;

import bio.terra.pearl.core.dao.document.StudyEnvironmentDocumentRequestDao;
import bio.terra.pearl.core.model.document.StudyEnvironmentDocumentRequest;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StudyEnvironmentDocumentRequestService extends CrudService<StudyEnvironmentDocumentRequest, StudyEnvironmentDocumentRequestDao> {

    public StudyEnvironmentDocumentRequestService(StudyEnvironmentDocumentRequestDao dao) {
        super(dao);
    }

    public Optional<StudyEnvironmentDocumentRequest> findByDocumentRequestId(UUID studyEnvId, UUID documentRequestId) {
        return dao.findByDocumentRequestId(studyEnvId, documentRequestId);
    }

    public List<StudyEnvironmentDocumentRequest> findActiveByStudyEnvironmentId(UUID studyEnvId) {
        return dao.findActiveByStudyEnvironmentId(studyEnvId);
    }
}
