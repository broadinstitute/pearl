package bio.terra.pearl.core.service.fileupload;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.fileupload.ParticipantFileUpload;
import bio.terra.pearl.core.service.fileupload.backends.LocalFileStorageBackend;
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

class ParticipantFileUploadServiceTest extends BaseSpringBootTest {


    @MockBean
    private LocalFileStorageBackend localFileStorageBackend;

    @Autowired
    private ParticipantFileUploadService participantFileUploadService;

    @Autowired
    private EnrolleeFactory enrolleeFactory;

    @Test
    @Transactional
    void uploadFileAndCreate(TestInfo info) {

        UUID uuid = UUID.randomUUID();

        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info));

        // Mocking the uploadFile method of LocalFileStorageBackend
        when(localFileStorageBackend.uploadFile(any())).thenReturn(uuid);

        ParticipantFileUpload pfu = participantFileUploadService.uploadFileAndCreate(
                ParticipantFileUpload
                        .builder()
                        .fileName("my_file.txt")
                        .fileType("txt")
                        .creatingPortalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
                        .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
                        .build(),
                new ByteArrayInputStream("my file".getBytes()));

        pfu =  participantFileUploadService.find(pfu.getId()).get();

        assertEquals("my_file.txt", pfu.getFileName());
        assertEquals("txt", pfu.getFileType());
        assertEquals(enrolleeBundle.portalParticipantUser().getId(), pfu.getCreatingPortalParticipantUserId());
        assertEquals(enrolleeBundle.portalParticipantUser().getId(), pfu.getPortalParticipantUserId());
        assertEquals(uuid, pfu.getUploadedFileId());
    }

}
