package bio.terra.pearl.api.admin.service.siteContent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.site.SiteContentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.site.LocalizedSiteContent;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.portal.PortalEnvironmentLanguageService;
import bio.terra.pearl.core.service.site.SiteContentService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SiteContentExtServiceTests extends BaseSpringBootTest {
  @Autowired private SiteContentExtService siteContentExtService;
  @Autowired private PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired private SiteContentFactory siteContentFactory;
  @Autowired private SiteContentService siteContentService;
  @Autowired private PortalEnvironmentLanguageService portalEnvironmentLanguageService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        siteContentExtService,
        Map.of(
            "get",
            AuthAnnotationSpec.withPortalPerm(AuthUtilService.BASE_PERMISSION),
            "getCurrent",
            AuthAnnotationSpec.withPortalEnvPerm(AuthUtilService.BASE_PERMISSION),
            "create",
            AuthAnnotationSpec.withPortalPerm("site_content_edit"),
            "versionList",
            AuthAnnotationSpec.withPortalPerm(AuthUtilService.BASE_PERMISSION)));
  }

  @Test
  @Transactional
  public void testGetLoadAllLanguages(TestInfo testInfo) {
    PortalEnvironment portalEnv =
        portalEnvironmentFactory.buildPersisted(getTestName(testInfo), EnvironmentName.sandbox);
    portalEnvironmentLanguageService.create(
        PortalEnvironmentLanguage.builder()
            .languageCode("es")
            .languageName("Spanish")
            .portalEnvironmentId(portalEnv.getId())
            .build());
    portalEnvironmentLanguageService.create(
        PortalEnvironmentLanguage.builder()
            .languageCode("en")
            .languageName("English")
            .portalEnvironmentId(portalEnv.getId())
            .build());

    SiteContent siteContent =
        SiteContent.builder()
            .portalId(portalEnv.getPortalId())
            .stableId(getTestName(testInfo))
            .version(1)
            .localizedSiteContents(
                List.of(
                    LocalizedSiteContent.builder()
                        .language("es")
                        .navLogoCleanFileName("spanish-logo.png")
                        .build(),
                    LocalizedSiteContent.builder()
                        .language("en")
                        .navLogoCleanFileName("english-logo.png")
                        .build()))
            .build();
    siteContent = siteContentService.create(siteContent);

    SiteContent loadedSiteContent =
        siteContentExtService.loadSiteContent(portalEnv, siteContent.getId()).orElseThrow();
    assertThat(loadedSiteContent.getLocalizedSiteContents(), hasSize(2));
    assertThat(
        loadedSiteContent.getLocalizedSiteContents().stream()
            .map(LocalizedSiteContent::getNavLogoCleanFileName)
            .toList(),
        containsInAnyOrder("spanish-logo.png", "english-logo.png"));
  }
}
