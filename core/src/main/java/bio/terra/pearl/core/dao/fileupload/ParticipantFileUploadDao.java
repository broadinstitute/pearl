package bio.terra.pearl.core.dao.fileupload;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.fileupload.ParticipantFileUpload;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class ParticipantFileUploadDao extends BaseJdbiDao<ParticipantFileUpload> {
    public ParticipantFileUploadDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ParticipantFileUpload> getClazz() {
        return ParticipantFileUpload.class;
    }

}
