package bio.terra.juniper.core.model.maintenance;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class SystemSettings extends BaseEntity {
    private String maintenanceModeMessage;
    private String maintenanceModeBypassPhrase;
    private boolean maintenanceModeEnabled;
    private boolean disableScheduledJobs;
}
