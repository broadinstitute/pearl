package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.notification.*;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.notification.TriggerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class TriggerActionServiceTests extends BaseSpringBootTest {
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private TriggerService triggerService;
    @Autowired
    private ParticipantTaskService participantTaskService;
    @Autowired
    private EventService eventService;
    @Autowired
    private KitTypeFactory kitTypeFactory;

    @Test
    @Transactional
    public void testUpdateTaskStatus(TestInfo testInfo) {
        // simulate updating an survey task on completion of a kit request
        EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        ParticipantTask task = createTask(enrolleeBundle, "exampleTask", TaskStatus.NEW);
        ParticipantTask otherTask = createTask(enrolleeBundle, "otherTask", TaskStatus.NEW);
        EnrolleeBundle otherEnrollee = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        ParticipantTask otherEnrolleeTask = createTask(otherEnrollee, "exampleTask", TaskStatus.NEW);

        // simulate updating an survey task on completion of a kit request
        Trigger config = createStatusTrigger(enrolleeBundle, TriggerEventType.KIT_SENT);
        config.setStatusToUpdateTo(TaskStatus.COMPLETE);
        config.setActionTargetStableIds(List.of("exampleTask"));
        triggerService.update(config);

        KitRequest kitRequest = createKitRequest(enrolleeBundle, getTestName(testInfo));
        eventService.publishKitStatusEvent(kitRequest, enrolleeBundle.enrollee(), enrolleeBundle.portalParticipantUser(),
                KitRequestStatus.SENT);

        // target task should be updated, other tasks should not
        task = participantTaskService.find(task.getId()).orElseThrow();
        assertThat(task.getStatus(), equalTo(TaskStatus.COMPLETE));
        otherTask = participantTaskService.find(otherTask.getId()).orElseThrow();
        assertThat(otherTask.getStatus(), equalTo(TaskStatus.NEW));
        otherEnrolleeTask = participantTaskService.find(otherEnrolleeTask.getId()).orElseThrow();
        assertThat(otherEnrolleeTask.getStatus(), equalTo(TaskStatus.NEW));
    }

    @Test
    @Transactional
    public void testTargetStableIdFilter(TestInfo testInfo) {
        // simulate sending a reminder email for a single survey type
        EnrolleeBundle enrolleeBundle = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        ParticipantTask targetTask = createTask(enrolleeBundle, "targetedSurvey", TaskStatus.NEW);
        ParticipantTask otherTask = createTask(enrolleeBundle, "otherSurvey", TaskStatus.NEW);
        ParticipantTask updateTask = createTask(enrolleeBundle, "updateSurvey", TaskStatus.NEW);
        EnrolleeBundle otherEnrollee = enrolleeFactory
                .buildWithPortalUser(getTestName(testInfo));
        ParticipantTask otherOtherTask = createTask(otherEnrollee, "otherSurvey", TaskStatus.NEW);
        ParticipantTask otherUpdateTask = createTask(otherEnrollee, "updateSurvey", TaskStatus.NEW);

        // simulate updating an survey task on completion of another survey
        Trigger config = createStatusTrigger(enrolleeBundle, TriggerEventType.SURVEY_RESPONSE);
        config.setStatusToUpdateTo(TaskStatus.COMPLETE);
        config.setFilterTargetStableIds(List.of("targetedSurvey"));
        config.setActionTargetStableIds(List.of("updateSurvey"));
        triggerService.update(config);

        // confirm that a survey event on the other survey does not trigger the status change
        eventService.publishEnrolleeSurveyEvent(enrolleeBundle.enrollee(), new SurveyResponse(),
                enrolleeBundle.portalParticipantUser(),
                otherTask);
        updateTask = participantTaskService.find(updateTask.getId()).orElseThrow();
        assertThat(updateTask.getStatus(), equalTo(TaskStatus.NEW));

        // confirm that a survey event on the targeted survey does trigger the status change
        eventService.publishEnrolleeSurveyEvent(enrolleeBundle.enrollee(), new SurveyResponse(),
                enrolleeBundle.portalParticipantUser(), targetTask);
        updateTask = participantTaskService.find(updateTask.getId()).orElseThrow();
        assertThat(updateTask.getStatus(), equalTo(TaskStatus.COMPLETE));

        // confirm the other enrollee's task is not updated
        otherUpdateTask = participantTaskService.find(otherUpdateTask.getId()).orElseThrow();
        assertThat(otherUpdateTask.getStatus(), equalTo(TaskStatus.NEW));
    }

    private ParticipantTask createTask(EnrolleeBundle enrolleeBundle, String taskStableId, TaskStatus status ) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        ParticipantTask task = ParticipantTask.builder()
                .enrolleeId(enrollee.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
                .targetStableId(taskStableId)
                .taskType(TaskType.SURVEY)
                .status(status)
                .build();
        task = participantTaskService.create(task, null);
        return task;
    }

    private Trigger createStatusTrigger(EnrolleeBundle enrolleeBundle, TriggerEventType eventType) {
        Enrollee enrollee = enrolleeBundle.enrollee();
        Trigger config = Trigger.builder()
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .eventType(eventType)
                .triggerType(TriggerType.EVENT)
                .actionType(TriggerActionType.TASK_STATUS_CHANGE)
                .portalEnvironmentId(enrolleeBundle.portalParticipantUser().getPortalEnvironmentId())
                .build();
        config = triggerService.create(config);
        return config;
    }

    private KitRequest createKitRequest(EnrolleeBundle enrolleeBundle, String testName) {
        KitRequest kitRequest = KitRequest.builder()
                .status(KitRequestStatus.SENT)
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .kitType(kitTypeFactory.buildPersisted(testName))
                .build();
        return kitRequest;
    }


}
