package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentLanguageFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dashboard.AlertTrigger;
import bio.terra.pearl.core.model.dashboard.AlertType;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerEventType;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.publishing.ParticipantDashboardAlertChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentLanguageService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PortalPublishingServiceTests extends BaseSpringBootTest {

    @Test
    public void testDiffBothUninitialized() {
        PortalEnvironment sourceEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder().initialized(false).build()
        ).build();
        PortalEnvironment destEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder().initialized(false).build()
        ).build();
        PortalEnvironmentChange changeRecord = portalPublishingService.diffPortalEnvs(sourceEnv, destEnv);
        assertThat(changeRecord.getConfigChanges(), hasSize(0));
        assertThat(changeRecord.getSiteContentChange().isChanged(), equalTo(false));
        assertThat(changeRecord.getPreRegSurveyChanges().isChanged(), equalTo(false));
    }

    @Test
    public void testDiffDestUninitialized() {
        PortalEnvironment sourceEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                        PortalEnvironmentConfig.builder()
                                .emailSourceAddress("blah@blah.com")
                                .initialized(true).build())
                .siteContent(SiteContent.builder().stableId("contentA").version(1).build())
                .preRegSurvey(Survey.builder().stableId("survA").version(1).build())
                .triggers(List.of(Trigger.builder().triggerType(TriggerType.EVENT)
                        .eventType(TriggerEventType.STUDY_CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("foo").build()).build()))
                .build();
        PortalEnvironment destEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder().initialized(false).build()
        ).build();
        PortalEnvironmentChange changeRecord = portalPublishingService.diffPortalEnvs(sourceEnv, destEnv);
        assertThat(changeRecord.getConfigChanges(), hasSize(2));
        assertThat(changeRecord.getSiteContentChange().isChanged(), equalTo(true));
        assertThat(changeRecord.getPreRegSurveyChanges().isChanged(), equalTo(true));
        assertThat(changeRecord.getTriggerChanges().addedItems(), hasSize(1));
    }

    @Test
    @Transactional
    public void testApplyPortalConfigChanges(TestInfo info) {
        AdminUser user = adminUserFactory.buildPersisted(getTestName(info), true);
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        PortalEnvironment irbEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.irb, portal.getId());
        PortalEnvironment liveEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.live, portal.getId());

        PortalEnvironmentConfig irbConfig = portalEnvironmentConfigService.find(irbEnv.getPortalEnvironmentConfigId()).get();
        irbConfig.setPassword("foobar");
        irbConfig.setEmailSourceAddress("info@demo.com");
        portalEnvironmentConfigService.update(irbConfig);

        // simulate the irb environment having English and Spanish languages, but live just having english
        portalEnvironmentLanguageFactory.addToEnvironment(irbEnv.getId(), "English", "en");
        portalEnvironmentLanguageFactory.addToEnvironment(irbEnv.getId(), "Spanish", "es");
        portalEnvironmentLanguageFactory.addToEnvironment(liveEnv.getId(), "English", "en");


        PortalEnvironmentChange changes = portalPublishingService.diffPortalEnvs(portal.getShortcode(), EnvironmentName.irb, EnvironmentName.live);
        portalPublishingService.applyChanges(portal.getShortcode(), EnvironmentName.live, changes, user);
        PortalEnvironmentConfig liveConfig = portalEnvironmentConfigService.find(liveEnv.getPortalEnvironmentConfigId()).get();
        assertThat(liveConfig.getPassword(), equalTo("foobar"));
        assertThat(liveConfig.getEmailSourceAddress(), equalTo("info@demo.com"));
    }

    @Test
    @Transactional
    public void testApplyPortalLanguageAddRemove(TestInfo info) {
        AdminUser user = adminUserFactory.buildPersisted(getTestName(info), true);
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        PortalEnvironment irbEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.irb, portal.getId());
        PortalEnvironment liveEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.live, portal.getId());

        // simulate the irb environment having English and Spanish languages, but live just having english
        portalEnvironmentLanguageFactory.addToEnvironment(irbEnv.getId(), "English", "en");
        PortalEnvironmentLanguage irbSpanish = portalEnvironmentLanguageFactory.addToEnvironment(irbEnv.getId(), "Spanish", "es");
        portalEnvironmentLanguageFactory.addToEnvironment(liveEnv.getId(), "English", "en");

        PortalEnvironmentChange changes = portalPublishingService.diffPortalEnvs(portal.getShortcode(), EnvironmentName.irb, EnvironmentName.live);
        portalPublishingService.applyChanges(portal.getShortcode(), EnvironmentName.live, changes, user);

        List<PortalEnvironmentLanguage> liveLanguages = portalEnvironmentLanguageService.findByPortalEnvId(liveEnv.getId());
        assertThat(liveLanguages.stream().map(PortalEnvironmentLanguage::getLanguageCode).toList(), containsInAnyOrder("en", "es"));

        // now delete spanish from the irb, and republish
        portalEnvironmentLanguageService.delete(irbSpanish.getId(), CascadeProperty.EMPTY_SET);
        changes = portalPublishingService.diffPortalEnvs(portal.getShortcode(), EnvironmentName.irb, EnvironmentName.live);

        portalPublishingService.applyChanges(portal.getShortcode(), EnvironmentName.live, changes, user);
        liveLanguages = portalEnvironmentLanguageService.findByPortalEnvId(liveEnv.getId());
        // live should now just contain english
        assertThat(liveLanguages.stream().map(PortalEnvironmentLanguage::getLanguageCode).toList(), contains("en"));
    }

    @Test
    @Transactional
    public void testPublishesSurveyPortalChanges(TestInfo info) {
        AdminUser user = adminUserFactory.buildPersisted(getTestName(info), true);
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        Survey survey = surveyFactory.buildPersisted(getTestName(info), portal.getId());
        PortalEnvironment irbEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.irb, portal.getId());
        PortalEnvironment liveEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.live, portal.getId());
        irbEnv.setPreRegSurveyId(survey.getId());

        portalEnvironmentService.update(irbEnv);
        survey.setPortalId(portal.getId());

        PortalEnvironmentChange changes = portalPublishingService.diffPortalEnvs(portal.getShortcode(), EnvironmentName.irb, EnvironmentName.live);
        portalPublishingService.applyChanges(portal.getShortcode(), EnvironmentName.live, changes, user);

        PortalEnvironment updatedLiveEnv = portalEnvironmentService.find(liveEnv.getId()).get();
        assertThat(updatedLiveEnv.getPreRegSurveyId(), equalTo(survey.getId()));
        survey = surveyService.find(survey.getId()).get();
        assertThat(survey.getPublishedVersion(), equalTo(1));
    }

    @Test
    @Transactional
    public void testApplyAlertChanges(TestInfo info) {
        AdminUser user = adminUserFactory.buildPersisted(getTestName(info), true);
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        PortalEnvironment irbEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.irb, portal.getId());
        PortalEnvironment liveEnv = portalEnvironmentFactory.buildPersisted(getTestName(info), EnvironmentName.live, portal.getId());
        ParticipantDashboardAlert alert = ParticipantDashboardAlert.builder()
                .title("No activities left!")
                .detail("This message shouldn't change")
                .alertType(AlertType.INFO)
                .portalEnvironmentId(irbEnv.getId())
                .trigger(AlertTrigger.NO_ACTIVITIES_REMAIN)
                .build();
        portalDashboardConfigService.create(alert);

        PortalEnvironmentChange changes = portalPublishingService.diffPortalEnvs(portal.getShortcode(), EnvironmentName.irb, EnvironmentName.live);
        portalPublishingService.applyChanges(portal.getShortcode(), liveEnv.getEnvironmentName(), changes, user);
        assertThat(portalDashboardConfigService.findByPortalEnvIdAndTrigger(liveEnv.getId(), AlertTrigger.NO_ACTIVITIES_REMAIN).get().getTitle(), equalTo("No activities left!"));
    }


    @Autowired
    private PortalDashboardConfigService portalDashboardConfigService;
    @Autowired
    private PortalPublishingService portalPublishingService;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private PortalFactory portalFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private AdminUserFactory adminUserFactory;
    @Autowired
    private PortalEnvironmentService portalEnvironmentService;
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private PortalEnvironmentConfigService portalEnvironmentConfigService;
    @Autowired
    private PortalEnvironmentLanguageFactory portalEnvironmentLanguageFactory;
    @Autowired
    private PortalEnvironmentLanguageService portalEnvironmentLanguageService;
}
