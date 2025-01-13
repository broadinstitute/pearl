package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public abstract class EnrolleeEvent implements BaseEvent {
    private Enrollee enrollee;
    private PortalParticipantUser portalParticipantUser;
    private EnrolleeContext enrolleeContext;

    /**
     * the targetStableId of a task that generated this event (e.g. the stableId of the survey that was completed)
     * will be null for events that are not task-specific (e.g. enrollment)
     * */
    public String getTargetStableId() {
        return null;
    }
}
