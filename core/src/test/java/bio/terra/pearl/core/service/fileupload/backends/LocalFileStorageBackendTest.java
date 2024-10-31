package bio.terra.pearl.core.service.fileupload.backends;

import bio.terra.pearl.core.service.fileupload.FileStorageConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LocalFileStorageBackendTest {

    @TempDir
    public File tempDir;

    @Test
    public void testUploadDownloadDeleteFile() throws IOException {
        LocalFileStorageBackend localFileStorageBackend = new LocalFileStorageBackend(createFileStorageConfig());

        InputStream data = new ByteArrayInputStream("test data".getBytes());

        // upload
        UUID fileId = localFileStorageBackend.uploadFile(data);

        // download
        InputStream downloadedData = localFileStorageBackend.downloadFile(fileId);

        // assert data is the same
        assertEquals("test data", new String(downloadedData.readAllBytes()));

        // delete
        localFileStorageBackend.deleteFile(fileId);

        // can't download
        downloadedData = localFileStorageBackend.downloadFile(fileId);
        assertNull(downloadedData);

    }


    private FileStorageConfig createFileStorageConfig() {
        MockEnvironment mockEnvironment =
                new MockEnvironment()
                        .withProperty("env.fileUpload.backend", "LocalFileStorageBackend")
                        .withProperty("env.fileUpload.localFileStoragePath", tempDir.getAbsolutePath());

        FileStorageConfig fileStorageConfig = new FileStorageConfig(mockEnvironment);
        fileStorageConfig.setLocalFileStoragePath(tempDir.getAbsolutePath());
        return fileStorageConfig;
    }


}
