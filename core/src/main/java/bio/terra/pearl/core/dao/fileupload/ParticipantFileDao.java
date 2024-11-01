package bio.terra.pearl.core.dao.fileupload;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.fileupload.ParticipantFile;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ParticipantFileDao extends BaseJdbiDao<ParticipantFile> {
    public ParticipantFileDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ParticipantFile> getClazz() {
        return ParticipantFile.class;
    }

}
