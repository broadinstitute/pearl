package bio.terra.pearl.core.service.workflow;

import java.util.UUID;

public interface TaskConfigCreatedEvent {
    String getStableId();
    UUID getStudyEnvironmentId();
    Integer getVersion();
}
