package bio.terra.pearl.core.service.file;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.core.service.file.backends.LocalFileStorageBackend;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ParticipantFileServiceTest extends BaseSpringBootTest {


    @MockBean
    private LocalFileStorageBackend localFileStorageBackend;

    @Autowired
    private ParticipantFileService participantFileService;

    @Autowired
    private EnrolleeFactory enrolleeFactory;

    @Test
    @Transactional
    void uploadFileAndCreate(TestInfo info) {

        UUID uuid = UUID.randomUUID();

        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info));

        // Mocking the uploadFile method of LocalFileStorageBackend
        when(localFileStorageBackend.uploadFile(any())).thenReturn(uuid);

        ParticipantFile pfu = participantFileService.uploadFileAndCreate(
                ParticipantFile
                        .builder()
                        .fileName("my_file.txt")
                        .fileType("txt")
                        .creatingParticipantUserId(enrolleeBundle.participantUser().getId())
                        .enrolleeId(enrolleeBundle.enrollee().getId())
                        .build(),
                new ByteArrayInputStream("my file".getBytes()));

        pfu = participantFileService.find(pfu.getId()).get();

        assertEquals("my_file.txt", pfu.getFileName());
        assertEquals("txt", pfu.getFileType());
        assertEquals(enrolleeBundle.participantUser().getId(), pfu.getCreatingParticipantUserId());
        assertEquals(enrolleeBundle.enrollee().getId(), pfu.getEnrolleeId());
        assertEquals(uuid, pfu.getExternalFileId());
    }

}
