package bio.terra.pearl.core.dao.document;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.StudyEnvAttachedDao;
import bio.terra.pearl.core.model.document.StudyEnvironmentDocumentRequest;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StudyEnvironmentDocumentRequestDao extends BaseMutableJdbiDao<StudyEnvironmentDocumentRequest>  implements StudyEnvAttachedDao<StudyEnvironmentDocumentRequest>  {
    public StudyEnvironmentDocumentRequestDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<StudyEnvironmentDocumentRequest> getClazz() {
        return StudyEnvironmentDocumentRequest.class;
    }


    public Optional<StudyEnvironmentDocumentRequest> findByDocumentRequestId(UUID studyEnvId, UUID documentRequestId) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT * FROM study_environment_document_request WHERE study_environment_id = :studyEnvId AND document_request_id = :documentRequestId AND active = true";
            return handle.createQuery(sql)
                    .bind("studyEnvId", studyEnvId)
                    .bind("documentRequestId", documentRequestId)
                    .mapTo(StudyEnvironmentDocumentRequest.class)
                    .findOne();
        });
    }

    public List<StudyEnvironmentDocumentRequest> findActiveByStudyEnvironmentId(UUID studyEnvId) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT * FROM study_environment_document_request WHERE study_environment_id = :studyEnvId AND active = true";
            return handle.createQuery(sql)
                    .bind("studyEnvId", studyEnvId)
                    .mapTo(StudyEnvironmentDocumentRequest.class)
                    .list();
        });
    }
}
