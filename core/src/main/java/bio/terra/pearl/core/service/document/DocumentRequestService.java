package bio.terra.pearl.core.service.document;

import bio.terra.pearl.core.dao.document.DocumentRequestDao;
import bio.terra.pearl.core.model.document.DocumentRequest;
import bio.terra.pearl.core.service.VersionedEntityService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentRequestService extends VersionedEntityService<DocumentRequest, DocumentRequestDao> {
    public DocumentRequestService(DocumentRequestDao dao) {
        super(dao);
    }

    public Optional<DocumentRequest> findActiveInStudyEnvByStableId(String stableId, Integer version) {
        return dao.findActiveInStudyEnvByStableId(stableId, version);
    }
}
