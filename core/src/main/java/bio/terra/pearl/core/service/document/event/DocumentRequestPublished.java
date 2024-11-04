package bio.terra.pearl.core.service.document.event;

import bio.terra.pearl.core.model.document.DocumentRequest;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.service.workflow.BaseEvent;
import bio.terra.pearl.core.service.workflow.TaskConfigCreatedEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents an update to enrollee survey data, most commonly by the participant submitting a completed survey
 * Eventually we might want separate events for e.g. completing a survey vs. updating one.  But for now we just have one
 */
@Getter @Setter
@SuperBuilder
public class DocumentRequestPublished extends Event implements BaseEvent, TaskConfigCreatedEvent {
    private DocumentRequest documentRequest;  // the survey that is being published -- the content does not need to be attached

    public String getStableId() {
        return documentRequest.getStableId();
    }

    public Integer getVersion() {
        return documentRequest.getVersion();
    }
}
