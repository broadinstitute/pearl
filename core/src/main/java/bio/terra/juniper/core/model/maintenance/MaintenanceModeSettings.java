package bio.terra.juniper.core.model.maintenance;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
public class MaintenanceModeSettings {
    private String message;
    private String bypassPhrase;
    private boolean enabled;
    private boolean disableScheduledJobs;
}
