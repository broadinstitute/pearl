package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.notification.NotificationDao;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.kit.KitTypeFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationDeliveryType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.participant.EnrolleeSourceType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.kit.pepper.PepperKitStatus;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * See ParticipantTaskDaoTests for detailed tests of the reminder timing logic.  This is a high-level test
 * to confirm everything works together.
 */
public class EnrolleeReminderServiceTests extends BaseSpringBootTest {
  @Test
  @Transactional
  public void testConsentReminders(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
      EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
    ParticipantTask newTask1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);

    Trigger config = Trigger.builder()
        .triggerType(TriggerType.TASK_REMINDER)
        .taskType(TaskType.CONSENT)
        .afterMinutesIncomplete(0)
        .deliveryType(NotificationDeliveryType.EMAIL)
        .studyEnvironmentId(studyEnv.getId())
        .portalEnvironmentId(portalEnv.getId())
        .build();
    Trigger savedConfig = triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(1));
    assertThat(notificationList.get(0).getTriggerId(), equalTo(savedConfig.getId()));
  }

  @Test
  @Transactional
  public void testSurveyRemindersDontSendToUnconsented(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv);
    ParticipantTask newTask1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.SURVEY);

    Trigger config = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.SURVEY)
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnv.getId())
            .portalEnvironmentId(portalEnv.getId())
            .build();
    triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(0));
  }

  @Test
  @Transactional
  public void testFiltersByKitType(TestInfo info) {
    String testName = getTestName(info);
    KitType bloodKitType = kitTypeFactory.buildPersisted("Blood");
    KitType salivaKitType = kitTypeFactory.buildPersisted("Saliva");
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);
    EnrolleeBundle enrolleeBundle1 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);
    EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);

    KitRequest kitRequest1 = kitRequestFactory.buildPersisted(testName, enrolleeBundle1.enrollee(), PepperKitStatus.CREATED, bloodKitType.getId());
    participantTaskFactory.buildPersisted(enrolleeBundle1, TaskStatus.NEW, kitRequest1.getId(), bloodKitType);

    KitRequest kitRequest2 = kitRequestFactory.buildPersisted(testName, enrolleeBundle2.enrollee(), PepperKitStatus.CREATED, salivaKitType.getId());
    participantTaskFactory.buildPersisted(enrolleeBundle2, TaskStatus.NEW, kitRequest2.getId(), salivaKitType);

    Trigger config = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.KIT_REQUEST)
            .filterTargetStableIds(List.of(bloodKitType.getName()))
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnv.getId())
            .portalEnvironmentId(portalEnv.getId())
            .build();
    triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle1.enrollee().getId());
    assertThat(notificationList, hasSize(1));
    notificationList = notificationDao.findByEnrolleeId(enrolleeBundle2.enrollee().getId());
    assertThat(notificationList, hasSize(0));
  }

  @Test
  @Transactional
  public void testSendsKitReminders(TestInfo info) {
    String testName = getTestName(info);
    KitType bloodKitType = kitTypeFactory.buildPersisted("Blood");
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, testName);

    //  create two enrollees, one with a pending kit request task, and one with a completed kit request task
    EnrolleeBundle enrolleeBundle1 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);
    EnrolleeBundle enrolleeBundle2 = enrolleeFactory.buildWithPortalUser(testName, portalEnv, studyEnv, true);

    KitRequest kitRequest1 = kitRequestFactory.buildPersisted(testName, enrolleeBundle1.enrollee(), PepperKitStatus.CREATED, bloodKitType.getId());
    participantTaskFactory.buildPersisted(enrolleeBundle1, TaskStatus.NEW, kitRequest1.getId(), bloodKitType);

    KitRequest kitRequest2 = kitRequestFactory.buildPersisted(testName, enrolleeBundle2.enrollee(), PepperKitStatus.CREATED, bloodKitType.getId());
    participantTaskFactory.buildPersisted(enrolleeBundle2, TaskStatus.COMPLETE, kitRequest2.getId(), bloodKitType);

    Trigger config = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.KIT_REQUEST)
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnv.getId())
            .portalEnvironmentId(portalEnv.getId())
            .build();
    triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    // only the pending task should receive a reminder
    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle1.enrollee().getId());
    assertThat(notificationList, hasSize(1));
    notificationList = notificationDao.findByEnrolleeId(enrolleeBundle2.enrollee().getId());
    assertThat(notificationList, hasSize(0));
  }


  @Test
  @Transactional
  public void testRemindersDontSendToImported(TestInfo info) {
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    EnrolleeBundle enrolleeBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnv, studyEnv, true);
    enrolleeBundle.enrollee().setSource(EnrolleeSourceType.IMPORT);
    enrolleeService.update(enrolleeBundle.enrollee());
    ParticipantTask newTask1 = participantTaskFactory.buildPersisted(enrolleeBundle, TaskStatus.NEW, TaskType.CONSENT);
    Trigger config = Trigger.builder()
            .triggerType(TriggerType.TASK_REMINDER)
            .taskType(TaskType.CONSENT)
            .afterMinutesIncomplete(0)
            .deliveryType(NotificationDeliveryType.EMAIL)
            .studyEnvironmentId(studyEnv.getId())
            .portalEnvironmentId(portalEnv.getId())
            .build();
    triggerService.create(config);
    enrolleeReminderService.sendTaskReminders(studyEnv);

    List<Notification> notificationList = notificationDao.findByEnrolleeId(enrolleeBundle.enrollee().getId());
    assertThat(notificationList, hasSize(0));
  }

  @Autowired
  private ParticipantTaskFactory participantTaskFactory;
  @Autowired
  private EnrolleeReminderService enrolleeReminderService;
  @Autowired
  private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired
  private PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired
  private NotificationDao notificationDao;
  @Autowired
  private TriggerService triggerService;
  @Autowired
  private EnrolleeFactory enrolleeFactory;
  @Autowired
  private EnrolleeService enrolleeService;
  @Autowired
  private KitTypeFactory kitTypeFactory;
  @Autowired
  private KitRequestFactory kitRequestFactory;
}
