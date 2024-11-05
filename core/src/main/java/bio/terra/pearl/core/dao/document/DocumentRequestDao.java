package bio.terra.pearl.core.dao.document;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.model.document.DocumentRequest;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DocumentRequestDao  extends BaseVersionedJdbiDao<DocumentRequest> {

    public DocumentRequestDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<DocumentRequest> getClazz() {
        return DocumentRequest.class;
    }

    public Optional<DocumentRequest> findActiveInStudyEnvByStableId(String stableId, Integer version) {
        return jdbi.withHandle(handle -> {
            String sql = "SELECT dr.* FROM document_request dr " +
                    "INNER JOIN study_environment_document_request sedr ON sedr.document_request_id = dr.id " +
                    "WHERE dr.stable_id = :stableId AND dr.version = :version AND sedr.active = true";
            return handle.createQuery(sql)
                    .bind("stableId", stableId)
                    .bind("version", version)
                    .mapTo(DocumentRequest.class)
                    .findOne();
        });
    }
}
