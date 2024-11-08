package bio.terra.pearl.core.service.fileupload;

import bio.terra.pearl.core.dao.fileupload.ParticipantFileDao;
import bio.terra.pearl.core.model.fileupload.ParticipantFile;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.fileupload.backends.FileStorageBackend;
import bio.terra.pearl.core.service.fileupload.backends.FileStorageBackendProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ParticipantFileService extends ImmutableEntityService<ParticipantFile, ParticipantFileDao> {
    private final FileStorageBackend fileStorageBackend;

    public ParticipantFileService(ParticipantFileDao dao, FileStorageBackendProvider fileStorageBackendProvider) {
        super(dao);

        this.fileStorageBackend = fileStorageBackendProvider.get();
    }

    public ParticipantFile uploadFileAndCreate(ParticipantFile participantFile, InputStream file) {
        UUID fileId = fileStorageBackend.uploadFile(file);
        participantFile.setExternalFileId(fileId);
        return dao.create(participantFile);
    }

    public List<ParticipantFile> findBySurveyResponseId(UUID surveyResponseId) {
        return dao.findBySurveyResponseId(surveyResponseId);
    }

    public List<ParticipantFile> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) {
        dao.deleteByEnrolleeId(enrolleeId);
    }
}
