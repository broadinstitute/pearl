package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.publishing.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.notification.EmailTemplateService;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** dedicated service for applying deltas to portal environments */
@Service
public class PortalUpdateService {
    private PortalDiffService portalDiffService;
    private PortalEnvironmentService portalEnvironmentService;
    private PortalEnvironmentConfigService portalEnvironmentConfigService;
    private PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao;
    private NotificationConfigService notificationConfigService;
    private SurveyService surveyService;
    private EmailTemplateService emailTemplateService;
    private SiteContentService siteContentService;
    private StudyUpdateService studyUpdateService;
    private ObjectMapper objectMapper;


    public PortalUpdateService(PortalDiffService portalDiffService,
                               PortalEnvironmentService portalEnvironmentService,
                               PortalEnvironmentConfigService portalEnvironmentConfigService,
                               PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao,
                               NotificationConfigService notificationConfigService, SurveyService surveyService,
                               EmailTemplateService emailTemplateService, SiteContentService siteContentService,
                               StudyUpdateService studyUpdateService, ObjectMapper objectMapper) {
        this.portalDiffService = portalDiffService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.portalEnvironmentConfigService = portalEnvironmentConfigService;
        this.portalEnvironmentChangeRecordDao = portalEnvironmentChangeRecordDao;
        this.notificationConfigService = notificationConfigService;
        this.surveyService = surveyService;
        this.emailTemplateService = emailTemplateService;
        this.siteContentService = siteContentService;
        this.studyUpdateService = studyUpdateService;
        this.objectMapper = objectMapper;
    }

    /** updates the dest environment with the given changes */
    @Transactional
    public PortalEnvironment applyChanges(String shortcode, EnvironmentName dest, PortalEnvironmentChange change, AdminUser user) throws Exception {
        PortalEnvironment destEnv = portalDiffService.loadPortalEnvForProcessing(shortcode, dest);
        return applyUpdate(destEnv, change, user);
    }

    /** applies the given update -- the destEnv provided must already be fully-hydrated from loadPortalEnv */
    protected PortalEnvironment applyUpdate(PortalEnvironment destEnv, PortalEnvironmentChange envChanges, AdminUser user) throws Exception {
        applyChangesToEnvConfig(destEnv, envChanges.configChanges());

        applyChangesToPreRegSurvey(destEnv, envChanges.preRegSurveyChanges());
        applyChangesToSiteContent(destEnv, envChanges.siteContentChange());
        applyChangesToNotificationConfigs(destEnv, envChanges.notificationConfigChanges());
        for(StudyEnvironmentChange studyEnvChange : envChanges.studyEnvChanges()) {
            StudyEnvironment studyEnv = portalDiffService.loadStudyEnvForProcessing(studyEnvChange.studyShortcode(), destEnv.getEnvironmentName());
            studyUpdateService.applyChanges(studyEnv, studyEnvChange, destEnv.getId());
        }

        var changeRecord = PortalEnvironmentChangeRecord.builder()
                .adminUserId(user.getId())
                .portalEnvironmentChange(objectMapper.writeValueAsString(envChanges))
                .build();
        portalEnvironmentChangeRecordDao.create(changeRecord);
        return destEnv;
    }

    /** updates the passed-in config with the given changes.  Returns the updated config */
    protected PortalEnvironmentConfig applyChangesToEnvConfig(PortalEnvironment destEnv,
                                                              List<ConfigChange> configChanges) throws Exception {
        if (configChanges.isEmpty()) {
            return destEnv.getPortalEnvironmentConfig();
        }
        for (ConfigChange change : configChanges) {
            PropertyUtils.setProperty(destEnv.getPortalEnvironmentConfig(), change.propertyName(), change.newValue());
        }
        return portalEnvironmentConfigService.update(destEnv.getPortalEnvironmentConfig());
    }

        protected PortalEnvironment applyChangesToPreRegSurvey(PortalEnvironment destEnv, VersionedEntityChange change) throws Exception {
            if (!change.isChanged()) {
                return destEnv;
            }
            UUID newSurveyId = null;
            if (change.newStableId() != null) {
                newSurveyId = surveyService.findByStableId(change.newStableId(), change.newVersion()).get().getId();
            }
            destEnv.setPreRegSurveyId(newSurveyId);
            return portalEnvironmentService.update(destEnv);
        }

    protected PortalEnvironment applyChangesToSiteContent(PortalEnvironment destEnv, VersionedEntityChange change) throws Exception {
        if (!change.isChanged()) {
            return destEnv;
        }
        UUID newDocumentId = null;
        if (change.newStableId() != null) {
            newDocumentId = siteContentService.findByStableId(change.newStableId(), change.newVersion()).get().getId();
        }
        destEnv.setSiteContentId(newDocumentId);
        return portalEnvironmentService.update(destEnv);
    }

    protected void applyChangesToNotificationConfigs(PortalEnvironment destEnv, ListChange<NotificationConfig,
            VersionedConfigChange> listChange) throws Exception {
        for(NotificationConfig config : listChange.addedItems()) {
            config.setPortalEnvironmentId(destEnv.getId());
            notificationConfigService.create(config.cleanForCopying());
            destEnv.getNotificationConfigs().add(config);
        }
        for(NotificationConfig config : listChange.removedItems()) {
            notificationConfigService.delete(config.getId(), CascadeProperty.EMPTY_SET);
            destEnv.getNotificationConfigs().remove(config);
        }
        for(VersionedConfigChange change : listChange.changedItems()) {
            StudyUpdateService.applyChangesToVersionedConfig(change, notificationConfigService, emailTemplateService);
        }
    }
}
