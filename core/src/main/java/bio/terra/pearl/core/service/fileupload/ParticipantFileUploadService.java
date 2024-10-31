package bio.terra.pearl.core.service.fileupload;

import bio.terra.pearl.core.dao.fileupload.ParticipantFileUploadDao;
import bio.terra.pearl.core.model.fileupload.ParticipantFileUpload;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.fileupload.backends.FileStorageBackend;
import bio.terra.pearl.core.service.fileupload.backends.FileStorageBackendProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Slf4j
public class ParticipantFileUploadService extends ImmutableEntityService<ParticipantFileUpload, ParticipantFileUploadDao> {
    private final FileStorageBackend fileStorageBackend;

    public ParticipantFileUploadService(ParticipantFileUploadDao dao, FileStorageBackendProvider fileStorageBackendProvider) {
        super(dao);

        this.fileStorageBackend = fileStorageBackendProvider.get();
    }

    public ParticipantFileUpload uploadFileAndCreate(ParticipantFileUpload participantFileUpload, InputStream file) {
        participantFileUpload.setUploadedFileId(fileStorageBackend.uploadFile(file));
        return dao.create(participantFileUpload);
    }
}
