package bio.terra.pearl.api.admin.service.portal;

import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChangeRecord;
import bio.terra.pearl.core.service.publishing.PortalEnvironmentChangeRecordService;
import bio.terra.pearl.core.service.publishing.PortalPublishingService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PortalPublishingExtService {
  private final PortalPublishingService portalPublishingService;
  private final PortalEnvironmentChangeRecordService portalEnvironmentChangeRecordService;

  public PortalPublishingExtService(
      PortalPublishingService portalPublishingService,
      PortalEnvironmentChangeRecordService portalEnvironmentChangeRecordService) {
    this.portalPublishingService = portalPublishingService;
    this.portalEnvironmentChangeRecordService = portalEnvironmentChangeRecordService;
  }

  /** anyone can see the difference between two environments */
  @EnforcePortalPermission(permission = "BASE")
  public PortalEnvironmentChange diff(
      PortalAuthContext authContext, EnvironmentName destEnv, EnvironmentName sourceEnv) {
    return portalPublishingService.diffPortalEnvs(
        authContext.getPortalShortcode(), destEnv, sourceEnv);
  }

  @EnforcePortalPermission(permission = "publish")
  public PortalEnvironment publish(
      PortalEnvAuthContext authContext, PortalEnvironmentChange change) {
    return portalPublishingService.applyChanges(
        authContext.getPortalShortcode(),
        authContext.getEnvironmentName(),
        change,
        authContext.getOperator());
  }

  @EnforcePortalPermission(permission = "BASE")
  public List<PortalEnvironmentChangeRecord> getChangeRecords(PortalAuthContext authContext) {
    return portalEnvironmentChangeRecordService.findByPortalId(authContext.getPortal().getId());
  }
}
