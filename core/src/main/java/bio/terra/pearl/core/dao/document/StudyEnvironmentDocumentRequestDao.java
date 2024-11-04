package bio.terra.pearl.core.dao.document;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.StudyEnvAttachedDao;
import bio.terra.pearl.core.model.document.StudyEnvironmentDocumentRequest;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentDocumentRequestDao extends BaseMutableJdbiDao<StudyEnvironmentDocumentRequest>  implements StudyEnvAttachedDao<StudyEnvironmentDocumentRequest>  {
    public StudyEnvironmentDocumentRequestDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<StudyEnvironmentDocumentRequest> getClazz() {
        return StudyEnvironmentDocumentRequest.class;
    }


}
