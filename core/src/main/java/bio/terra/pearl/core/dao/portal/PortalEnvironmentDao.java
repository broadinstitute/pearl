package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.site.SiteContentDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PortalEnvironmentDao extends BaseMutableJdbiDao<PortalEnvironment> {
    private PortalEnvironmentConfigDao portalEnvironmentConfigDao;
    private SiteContentDao siteContentDao;
    private SurveyDao surveyDao;
    private PortalEnvironmentLanguageDao portalEnvironmentLanguageDao;
    public PortalEnvironmentDao(Jdbi jdbi,
                                PortalEnvironmentConfigDao portalEnvironmentConfigDao,
                                SiteContentDao siteContentDao, SurveyDao surveyDao,
                                PortalEnvironmentLanguageDao portalEnvironmentLanguageDao) {
        super(jdbi);
        this.portalEnvironmentConfigDao = portalEnvironmentConfigDao;
        this.siteContentDao = siteContentDao;
        this.surveyDao = surveyDao;
        this.portalEnvironmentLanguageDao = portalEnvironmentLanguageDao;
    }

    @Override
    public Class<PortalEnvironment> getClazz() {
        return PortalEnvironment.class;
    }

    public List<PortalEnvironment> findByPortalWithConfigs(UUID portalId) {
        return findAllByPropertyWithChildren("portal_id", portalId, "portalEnvironmentConfigId",
                "portalEnvironmentConfig", portalEnvironmentConfigDao);
    }

    public Optional<PortalEnvironment> findOne(String shortcode, EnvironmentName environmentName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("a") + " from " + tableName
                                + " a join portal on portal_id = portal.id"
                                + " where portal.shortcode = :shortcode and environment_name = :environmentName")
                        .bind("shortcode", shortcode)
                        .bind("environmentName", environmentName)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    /** load with everything needed to display the participant-facing site */
    public Optional<PortalEnvironment> loadWithSiteContent(String shortcode,
                                                                      EnvironmentName environmentName,
                                                                      String language) {
        Optional<PortalEnvironment> portalEnvOpt = findOne(shortcode, environmentName);
        portalEnvOpt.ifPresent(portalEnv -> {
            PortalEnvironmentConfig envConfig = portalEnvironmentConfigDao.find(portalEnv.getPortalEnvironmentConfigId()).orElse(null);
            portalEnv.setPortalEnvironmentConfig(envConfig);
            String languageToLoad = StringUtils.defaultIfBlank(language, envConfig.getDefaultLanguage());
            portalEnv.setSiteContent(siteContentDao.findOneFull(portalEnv.getSiteContentId(), languageToLoad).orElse(null));
            if (portalEnv.getPreRegSurveyId() != null) {
                portalEnv.setPreRegSurvey(surveyDao.find(portalEnv.getPreRegSurveyId()).get());
            }
            List<PortalEnvironmentLanguage> portalEnvLanguages = portalEnvironmentLanguageDao.findByPortalEnvId(portalEnv.getId());
            portalEnv.getSupportedLanguages().addAll(portalEnvLanguages);
        });
        return portalEnvOpt;
    }

    /** load with everything needed to display the participant-facing site */
    public Optional<PortalEnvironment> loadWithEnvConfig(UUID portalEnvironmentId) {
        return findWithChild(portalEnvironmentId, "portal_environment_config_id",
                "portalEnvironmentConfig", portalEnvironmentConfigDao);
    }
}
