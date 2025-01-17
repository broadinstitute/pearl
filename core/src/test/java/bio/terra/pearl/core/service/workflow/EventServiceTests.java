package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.Event;
import bio.terra.pearl.core.model.workflow.EventClass;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class EventServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private EventService eventService;
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;

    @Test
    @Transactional
    public void testPersistsEnrolleeConsentEvent(TestInfo info) {
        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser(getTestName(info));
        Assertions.assertEquals(0, eventService.findAll().size());
        eventService.publishEnrolleeConsentEvent(
                bundle.enrollee(),
                bundle.portalParticipantUser()
                );

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        assertValidCreatedEventForEnrollee(createdEvent, EventClass.ENROLLEE_CONSENT_EVENT, bundle);
    }

    @Test
    @Transactional
    public void testPersistsEnrolleeCreationEvent() {
        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser("testPersistsEnrolleeCreationEvent");
        Assertions.assertEquals(0, eventService.findAll().size());
        eventService.publishEnrolleeCreationEvent(
                bundle.enrollee(),
                bundle.portalParticipantUser());

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        assertValidCreatedEventForEnrollee(createdEvent, EventClass.ENROLLEE_CREATION_EVENT, bundle);
    }

    @Test
    @Transactional
    public void testPersistsEnrolleeSurveyEvent() {

        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser("testPersistsEnrolleeSurveyEvent");
        Assertions.assertEquals(0, eventService.findAll().size());
        eventService.publishEnrolleeSurveyEvent(
                bundle.enrollee(),
                SurveyResponse.builder().build(),
                bundle.portalParticipantUser(),
                new ParticipantTask());

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        assertValidCreatedEventForEnrollee(createdEvent, EventClass.ENROLLEE_SURVEY_EVENT, bundle);
    }

    @Test
    @Transactional
    public void testPersistsKitStatusEvent() {
        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser("testPersistsKitStatusEvent");
        Assertions.assertEquals(0, eventService.findAll().size());
        eventService.publishKitStatusEvent(
                kitRequestFactory.buildPersisted("testPersistsKitStatusEvent", bundle.enrollee()),
                bundle.enrollee(),
                bundle.portalParticipantUser(),
                KitRequestStatus.CREATED);

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        assertValidCreatedEventForEnrollee(createdEvent, EventClass.KIT_STATUS_EVENT, bundle);
    }

    @Test
    @Transactional
    public void testPersistsPublishPortalRegistrationEvent(TestInfo info) {

        EnrolleeBundle bundle = enrolleeFactory.buildWithPortalUser("testPersistsPublishPortalRegistrationEvent");
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
        Assertions.assertEquals(0, eventService.findAll().size());

        eventService.publishPortalRegistrationEvent(
                bundle.portalParticipantUser().getParticipantUser(),
                bundle.portalParticipantUser(),
                portalEnv);

        List<Event> createdEvents = eventService.findAll();
        Assertions.assertEquals(1, createdEvents.size());

        Event createdEvent = createdEvents.get(0);
        Assertions.assertNotNull(createdEvent.getCreatedAt());
        Assertions.assertEquals(EventClass.PORTAL_REGISTRATION_EVENT, createdEvent.getEventClass());
        Assertions.assertNull(createdEvent.getEnrolleeId());
        Assertions.assertEquals(portalEnv.getId(), createdEvent.getPortalEnvironmentId());
    }

    private void assertValidCreatedEventForEnrollee(Event created, EventClass eventClass, EnrolleeBundle bundle) {
        Assertions.assertNotNull(created.getCreatedAt());
        Assertions.assertEquals(eventClass, created.getEventClass());
        Assertions.assertEquals(bundle.enrollee().getId(), created.getEnrolleeId());
        Assertions.assertEquals(bundle.portalParticipantUser().getPortalEnvironmentId(), created.getPortalEnvironmentId());
    }
}
