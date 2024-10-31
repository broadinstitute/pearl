package bio.terra.pearl.core.service.fileupload;

import bio.terra.pearl.core.dao.fileupload.ParticipantFileUploadDao;
import bio.terra.pearl.core.model.fileupload.ParticipantFileUpload;
import bio.terra.pearl.core.service.ImmutableEntityService;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

public class ParticipantFileUploadService extends ImmutableEntityService<ParticipantFileUpload, ParticipantFileUploadDao> {
    public ParticipantFileUploadService(ParticipantFileUploadDao dao) {
        super(dao);
    }

    public Optional<ParticipantFileUpload> findForParticipantNoFileData(UUID portalParticipantUser, String fileName, Integer version) {
        return dao.findForParticipantNoFileData(portalParticipantUser, fileName, version);
    }

    public InputStream downloadFileContent(UUID portalParticipantUser, String fileName, Integer version) {
        // for now, fetch from database. in the future, we'll
        // fetch from a GCP bucket
        return dao.fetchFileContent(portalParticipantUser, fileName, version);
    }
}
