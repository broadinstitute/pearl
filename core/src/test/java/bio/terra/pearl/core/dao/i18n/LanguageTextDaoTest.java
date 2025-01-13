package bio.terra.pearl.core.dao.i18n;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.site.SiteContentFactory;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.LocalizedSiteContentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LanguageTextDaoTest extends BaseSpringBootTest {

    @Autowired
    private LanguageTextDao languageTextDao;

    @Autowired
    PortalEnvironmentFactory portalEnvironmentFactory;

    @Autowired
    SiteContentFactory siteContentFactory;

    @Autowired
    LocalizedSiteContentService localizedSiteContentService;

    @Autowired
    PortalEnvironmentService portalEnvironmentService;




    @Test
    @Transactional
    public void testFindForLocalSiteContentWithOverrides(TestInfo info) {


        LanguageText defaultProfileEn = LanguageText.builder()
                .language("en")
                .keyName("profile")
                .text("Profile")
                .build();
        LanguageText defaultProfileFr = LanguageText.builder()
                .language("fr")
                .keyName("profile")
                .text("Profil")
                .build();

        languageTextDao.create(defaultProfileEn);
        languageTextDao.create(defaultProfileFr);

        PortalEnvironment portalEnvironment = portalEnvironmentFactory.buildPersisted(getTestName(info));

        SiteContent siteContent = siteContentFactory.buildPersisted(
                siteContentFactory
                        .builder(getTestName(info))
                        .portalId(portalEnvironment.getPortalId())
                        .localizedSiteContents(List.of(
                                LocalizedSiteContent
                                        .builder()
                                        .language("en")
                                        .build()
                        ))
        );


        portalEnvironment.setSiteContentId(siteContent.getId());

        portalEnvironmentService.update(portalEnvironment);

        List<LanguageText> beforeOverride = languageTextDao
                .findWithOverridesByPortalEnvId(portalEnvironment.getId(), "en");

        LanguageText languageTextOverride = LanguageText.builder()
                .localizedSiteContentId(siteContent.getLocalizedSiteContents().get(0).getId())
                .language("en")
                .keyName("profile")
                .text("Something else!")
                .build();

        languageTextDao.create(languageTextOverride);

        List<LanguageText> afterOverride = languageTextDao
                .findWithOverridesByPortalEnvId(portalEnvironment.getId(), "en");

        List<LanguageText> otherLanguage = languageTextDao
                .findWithOverridesByPortalEnvId(portalEnvironment.getId(), "fr");

        LanguageText profileEn = beforeOverride.stream().filter(lt -> lt.getKeyName().equals("profile")).findFirst().orElseThrow();
        LanguageText profileEnOverride = afterOverride.stream().filter(lt -> lt.getKeyName().equals("profile")).findFirst().orElseThrow();
        LanguageText profileFr = otherLanguage.stream().filter(lt -> lt.getKeyName().equals("profile")).findFirst().orElseThrow();

        assertEquals("profile", profileEn.getKeyName());
        assertEquals("Profile", profileEn.getText());

        assertEquals("profile", profileEnOverride.getKeyName());
        assertEquals("Something else!", profileEnOverride.getText());

        assertEquals("profile", profileFr.getKeyName());
        assertEquals("Profil", profileFr.getText());


    }
}
