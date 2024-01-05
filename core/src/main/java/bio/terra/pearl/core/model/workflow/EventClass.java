package bio.terra.pearl.core.model.workflow;

import bio.terra.pearl.core.service.consent.EnrolleeConsentEvent;
import bio.terra.pearl.core.service.kit.KitStatusEvent;
import bio.terra.pearl.core.service.workflow.BaseEvent;
import bio.terra.pearl.core.service.workflow.EnrolleeCreationEvent;
import bio.terra.pearl.core.service.workflow.PortalRegistrationEvent;

public enum EventClass {
    ENROLLEE_CREATION_EVENT(EnrolleeCreationEvent.class),
    PORTAL_REGISTRATION_EVENT(PortalRegistrationEvent.class),
    ENROLLEE_SURVEY_EVENT(EnrolleeCreationEvent.class),
    ENROLLEE_CONSENT_EVENT(EnrolleeConsentEvent.class),
    KIT_STATUS_EVENT(KitStatusEvent.class);

    public final Class<? extends BaseEvent> eventClass;

    EventClass(Class<? extends BaseEvent> eventClass) {
        this.eventClass = eventClass;
    }
}
