package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerEventType;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.publishing.PortalEnvPublishable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TriggerServiceTests extends BaseSpringBootTest {
    @Autowired
    private TriggerService triggerService;

    @Test
    public void testIsVersionedConfigMatch() {
        // configs match if stableID of template is the same
        Trigger config = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_CONSENT)
                .emailTemplate(EmailTemplate.builder().stableId("foo").build()).build();

        Trigger configWithDifferentTemplate = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_ENROLLMENT)
                .emailTemplate(EmailTemplate.builder().stableId("foo").build()).build();
        assertThat(PortalEnvPublishable.isVersionedConfigMatch(config, configWithDifferentTemplate), equalTo(true));
    }

    @Test
    public void testIsVersionedConfigMatchDifferent() {
        Trigger config = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_CONSENT)
                .emailTemplate(EmailTemplate.builder().stableId("foo").build()).build();

        Trigger configWithDifferentTemplate = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_CONSENT)
                .emailTemplate(EmailTemplate.builder().stableId("bar").build()).build();
        assertThat(PortalEnvPublishable.isVersionedConfigMatch(config, configWithDifferentTemplate), equalTo(false));
    }

    @Test
    public void testDiffNotificationsNoEvents() throws Exception {
        List<Trigger> sourceList = List.of();
        List<Trigger> destList = List.of();
        var diffs = PortalEnvPublishable
                .diffConfigLists(sourceList, destList, triggerService.getPublishIgnoreProps());
        assertThat(diffs.addedItems(), hasSize(0));
        assertThat(diffs.changedItems(), hasSize(0));
        assertThat(diffs.removedItems(), hasSize(0));
    }

    @Test
    public void testDiffNotificationsOneEventMatched() {
        List<Trigger> sourceList = List.of(
                Trigger.builder().id(UUID.randomUUID())
                        .triggerType(TriggerType.EVENT)
                        .eventType(TriggerEventType.STUDY_CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("t1").build())
                        .build());
        List<Trigger> destList = List.of(
                Trigger.builder().id(UUID.randomUUID())
                        .triggerType(TriggerType.EVENT)
                        .eventType(TriggerEventType.STUDY_CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("t1").build())
                        .build());
        var diffs = PortalEnvPublishable
                .diffConfigLists(sourceList, destList, triggerService.getPublishIgnoreProps());
        assertThat(diffs.addedItems(), hasSize(0));
        assertThat(diffs.changedItems(), hasSize(0));
        assertThat(diffs.removedItems(), hasSize(0));
    }

    @Test
    public void testDiffNotificationsOneEventChanged() {
        List<Trigger> sourceList = List.of(
                Trigger.builder().id(UUID.randomUUID())
                        .triggerType(TriggerType.TASK_REMINDER)
                        .taskType(TaskType.CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("t1").build())
                        .afterMinutesIncomplete(3000)
                        .build());
        List<Trigger> destList = List.of(
                Trigger.builder().id(UUID.randomUUID())
                        .triggerType(TriggerType.TASK_REMINDER)
                        .taskType(TaskType.CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("t1").build())
                        .afterMinutesIncomplete(2000)
                        .build());
        var diffs = PortalEnvPublishable
                .diffConfigLists(sourceList, destList, triggerService.getPublishIgnoreProps());
        assertThat(diffs.addedItems(), hasSize(0));
        assertThat(diffs.changedItems(), hasSize(1));
        assertThat(diffs.changedItems().get(0).configChanges(), hasSize(1));
        assertThat(diffs.changedItems().get(0).configChanges().get(0), equalTo(new ConfigChange(
                "afterMinutesIncomplete", 2000, 3000
        )));
        assertThat(diffs.removedItems(), hasSize(0));
    }

    @Test
    public void testDiffNotificationsOneEventAdded() {
        Trigger addedConfig = Trigger.builder().id(UUID.randomUUID())
                .triggerType(TriggerType.TASK_REMINDER)
                .taskType(TaskType.CONSENT)
                .afterMinutesIncomplete(3000)
                .build();
        List<Trigger> sourceList = List.of(addedConfig);
        List<Trigger> destList = List.of();
        var diffs = PortalEnvPublishable
                .diffConfigLists(sourceList, destList, triggerService.getPublishIgnoreProps());
        assertThat(diffs.addedItems(), hasSize(1));
        assertThat(diffs.addedItems().get(0), samePropertyValuesAs(addedConfig));
        assertThat(diffs.changedItems(), hasSize(0));
        assertThat(diffs.removedItems(), hasSize(0));
    }

    @Test
    public void testDiffNotificationsOneEventRemoved() {
        Trigger removedConfig = Trigger.builder().id(UUID.randomUUID())
                .triggerType(TriggerType.TASK_REMINDER)
                .taskType(TaskType.CONSENT)
                .afterMinutesIncomplete(3000)
                .build();
        List<Trigger> sourceList = List.of();
        List<Trigger> destList = List.of(removedConfig);
        var diffs = PortalEnvPublishable
                .diffConfigLists(sourceList, destList, triggerService.getPublishIgnoreProps());
        assertThat(diffs.addedItems(), hasSize(0));
        assertThat(diffs.changedItems(), hasSize(0));
        assertThat(diffs.removedItems(), hasSize(1));
        assertThat(diffs.removedItems().get(0), samePropertyValuesAs(removedConfig));
    }
}
