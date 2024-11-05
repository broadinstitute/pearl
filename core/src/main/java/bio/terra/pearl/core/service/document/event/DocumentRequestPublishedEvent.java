package bio.terra.pearl.core.service.document.event;

import bio.terra.pearl.core.model.document.DocumentRequest;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.service.workflow.BaseEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Event indicating that a document request has been published
 */
@Getter
@Setter
@SuperBuilder
public class DocumentRequestPublishedEvent extends Event implements BaseEvent {
    private DocumentRequest documentRequest;
    private UUID documentRequestId;
}
