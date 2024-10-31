package bio.terra.pearl.core.service.fileupload;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileStorageConfig {
    private String defaultBackend;
    private String localFileStoragePath;
}
