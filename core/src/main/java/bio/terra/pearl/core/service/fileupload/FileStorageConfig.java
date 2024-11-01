package bio.terra.pearl.core.service.fileupload;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Getter
@Setter
@Configuration
public class FileStorageConfig {
    private String defaultBackend;
    private String localFileStoragePath;

    public FileStorageConfig(Environment environment) {
        this.defaultBackend = environment.getProperty("env.fileUpload.backend", "LocalFileStorageBackend");
        this.localFileStoragePath = environment.getProperty("env.fileUpload.localFileStoragePath");
    }
}
