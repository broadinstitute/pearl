package bio.terra.pearl.api.admin.service.siteContent;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnvPermission;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.portal.PortalEnvironmentLanguageService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.site.SiteContentService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SiteContentExtService {
  private final AuthUtilService authUtilService;
  private final SiteContentService siteContentService;
  private final PortalEnvironmentLanguageService portalEnvironmentLanguageService;
  private final PortalEnvironmentService portalEnvironmentService;

  public SiteContentExtService(
      AuthUtilService authUtilService,
      SiteContentService siteContentService,
      PortalEnvironmentLanguageService portalEnvironmentLanguageService,
      PortalEnvironmentService portalEnvironmentService) {
    this.authUtilService = authUtilService;
    this.siteContentService = siteContentService;
    this.portalEnvironmentLanguageService = portalEnvironmentLanguageService;
    this.portalEnvironmentService = portalEnvironmentService;
  }

  @EnforcePortalPermission(permission = AuthUtilService.BASE_PERMISSION)
  public Optional<SiteContent> get(
      PortalAuthContext authContext, String stableId, Integer version) {
    Portal portal = authContext.getPortal();
    PortalEnvironment portalEnv =
        portalEnvironmentService
            .findOne(portal.getShortcode(), EnvironmentName.sandbox)
            .orElseThrow();

    Optional<SiteContent> siteContentOpt =
        siteContentService.findByStableId(stableId, version, portal.getId());
    if (siteContentOpt.isEmpty()) {
      return Optional.empty();
    }
    return loadSiteContent(portalEnv, siteContentOpt.get().getId());
  }

  @EnforcePortalEnvPermission(permission = AuthUtilService.BASE_PERMISSION)
  public Optional<SiteContent> getCurrent(PortalEnvAuthContext authContext) {
    PortalEnvironment portalEnv = authContext.getPortalEnvironment();
    return loadSiteContent(portalEnv, portalEnv.getSiteContentId());
  }

  protected Optional<SiteContent> loadSiteContent(PortalEnvironment portalEnv, UUID siteContentId) {
    Optional<SiteContent> siteContentOpt = siteContentService.find(siteContentId);
    List<PortalEnvironmentLanguage> languages =
        portalEnvironmentLanguageService.findByPortalEnvId(portalEnv.getId());
    if (siteContentOpt.isPresent()
        && siteContentOpt.get().getPortalId().equals(portalEnv.getPortalId())) {
      for (PortalEnvironmentLanguage lang : languages) {
        siteContentService.attachChildContent(siteContentOpt.get(), lang.getLanguageCode());
      }
      return siteContentOpt;
    }
    return Optional.empty();
  }

  @EnforcePortalPermission(permission = "site_content_edit")
  public SiteContent create(
      PortalAuthContext authContext, String stableId, SiteContent siteContent) {
    siteContent.setPortalId(authContext.getPortal().getId());
    siteContent.setStableId(stableId);
    SiteContent newSiteContent = siteContentService.createNewVersion(siteContent);
    return newSiteContent;
  }

  @EnforcePortalPermission(permission = AuthUtilService.BASE_PERMISSION)
  public List<SiteContent> versionList(PortalAuthContext authContext, String stableId) {
    Portal portal = authContext.getPortal();
    List<SiteContent> contents = siteContentService.findByStableId(stableId, portal.getId());
    // filter out any that aren't associated with this portal
    List<SiteContent> contentsInPortal =
        contents.stream().filter(content -> content.getPortalId().equals(portal.getId())).toList();
    return contentsInPortal;
  }
}
