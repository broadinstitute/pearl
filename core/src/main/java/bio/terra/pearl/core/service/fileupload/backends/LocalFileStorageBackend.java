package bio.terra.pearl.core.service.fileupload.backends;

import bio.terra.pearl.core.service.fileupload.FileStorageConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

public class LocalFileStorageBackend implements FileStorageBackend {
    private final String localFileStoragePath;

    public LocalFileStorageBackend(FileStorageConfig fileStorageConfig) {
        this.localFileStoragePath = fileStorageConfig.getLocalFileStoragePath();
    }

    @Override
    public UUID uploadFile(InputStream data) {
        UUID fileId = UUID.randomUUID();

        File file = new File(localFileStoragePath + "/" + fileId);

        try {
            FileUtils.copyInputStreamToFile(data, file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to local storage", e);
        }

        return fileId;
    }

    @Override
    public InputStream downloadFile(UUID uploadedFileId) {
        return null;
    }

    @Override
    public void deleteFile(UUID uploadedFileId) {

    }
}
