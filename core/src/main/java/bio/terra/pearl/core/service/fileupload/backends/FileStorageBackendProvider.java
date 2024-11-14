package bio.terra.pearl.core.service.fileupload.backends;

import bio.terra.pearl.core.service.fileupload.FileStorageConfig;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FileStorageBackendProvider {

    private final Map<String, FileStorageBackend> backendMap = new HashMap<>();
    private final String defaultBackend;

    public FileStorageBackendProvider(
            FileStorageConfig fileStorageConfig,
            LocalFileStorageBackend localFileStorageBackend) {
        defaultBackend = fileStorageConfig.getDefaultBackend();
        backendMap.put("LocalFileStorageBackend", localFileStorageBackend);
    }

    public FileStorageBackend get() {
        return backendMap.get(defaultBackend);
    }

    public FileStorageBackend get(String client) {
        return backendMap.get(client);
    }

}
