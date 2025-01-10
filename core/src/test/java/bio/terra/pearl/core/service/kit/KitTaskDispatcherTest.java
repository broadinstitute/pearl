package bio.terra.pearl.core.service.kit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
import bio.terra.pearl.core.service.workflow.EventService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

class KitTaskDispatcherTest extends BaseSpringBootTest {

    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;
    @Autowired
    private KitRequestService kitRequestService;
    @Autowired
    private ParticipantTaskService taskService;
    @Autowired
    private EventService eventService;

    @Test
    @Transactional
    void testKitSentAndReceivedEventHandler(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(testName);

        KitRequest kitRequest = kitRequestFactory.buildPersisted(testName, enrolleeBundle.enrollee(), PepperKitStatus.SENT);

        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.CREATED);

        ParticipantTask task = taskService.findByKitRequestId(kitRequest.getId()).get();
        assertThat(task.getStatus(), equalTo(TaskStatus.NEW));


        // now check the received event completes the task
        kitRequest.setStatus(KitRequestStatus.RECEIVED);
        kitRequest = kitRequestService.update(kitRequest);
        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.SENT);
        task = taskService.findByKitRequestId(kitRequest.getId()).get();
        assertThat(task.getStatus(), equalTo(TaskStatus.COMPLETE));


        // now check that the reset works
        kitRequest = kitRequestFactory.buildPersisted(testName, enrolleeBundle.enrollee(), PepperKitStatus.SENT);
        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.RECEIVED);
        task = taskService.findByKitRequestId(kitRequest.getId()).get();
        assertThat(task.getStatus(), equalTo(TaskStatus.NEW));
    }
}
