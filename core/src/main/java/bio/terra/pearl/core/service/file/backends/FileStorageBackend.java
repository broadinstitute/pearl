package bio.terra.pearl.core.service.file.backends;

import java.io.InputStream;
import java.util.UUID;

public interface FileStorageBackend {


    /**
     * Upload file to backend. Returns unique identifier for the file.
     */
    UUID uploadFile(InputStream data);

    /**
     * Download file from backend.
     */
    InputStream downloadFile(UUID uploadedFileId);

    /**
     * Delete file from backend.
     */
    void deleteFile(UUID uploadedFileId);
}
