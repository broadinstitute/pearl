package bio.terra.pearl.core.service.workflow;

import java.util.UUID;

public interface TaskDispatchConfigCreatedEvent {
    String getStableId();
    UUID getStudyEnvironmentId();
}
