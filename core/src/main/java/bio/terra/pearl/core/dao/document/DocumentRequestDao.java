package bio.terra.pearl.core.dao.document;

import bio.terra.pearl.core.dao.BaseVersionedJdbiDao;
import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.model.document.DocumentRequest;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DocumentRequestDao  extends BaseVersionedJdbiDao<DocumentRequest> {

    public DocumentRequestDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<DocumentRequest> getClazz() {
        return DocumentRequest.class;
    }
}
