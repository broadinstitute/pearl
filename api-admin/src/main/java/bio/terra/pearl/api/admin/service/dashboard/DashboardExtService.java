package bio.terra.pearl.api.admin.service.dashboard;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnvPermission;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dashboard.AlertTrigger;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.exception.PortalEnvironmentMissing;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DashboardExtService {

  private PortalDashboardConfigService portalDashboardConfigService;
  private PortalEnvironmentService portalEnvironmentService;
  private AuthUtilService authUtilService;

  public DashboardExtService(
      PortalDashboardConfigService portalDashboardConfigService,
      PortalEnvironmentService portalEnvironmentService,
      AuthUtilService authUtilService) {
    this.portalDashboardConfigService = portalDashboardConfigService;
    this.portalEnvironmentService = portalEnvironmentService;
    this.authUtilService = authUtilService;
  }

  @EnforcePortalEnvPermission(permission = "BASE")
  public List<ParticipantDashboardAlert> listPortalEnvAlerts(
          PortalEnvAuthContext authContext) {
    return portalDashboardConfigService.findByPortalEnvId(authContext.getPortalEnvironment().getId());
  }

  @EnforcePortalEnvPermission(permission = "study_settings_edit")
  @SandboxOnly
  public ParticipantDashboardAlert updatePortalEnvAlert(
      PortalEnvAuthContext authContext,
      AlertTrigger triggerName,
      ParticipantDashboardAlert newAlert) {

    if (!triggerName.equals(newAlert.getTrigger())) {
      throw new IllegalArgumentException(
          "Trigger name specified in payload does not match trigger name specified in path");
    }

    ParticipantDashboardAlert alert =
        portalDashboardConfigService
            .findByPortalEnvIdAndTrigger(authContext.getPortalEnvironment().getId(), triggerName)
            .orElseThrow(() -> new NotFoundException("The specified alert does not exist"));

    alert.setTitle(newAlert.getTitle());
    alert.setDetail(newAlert.getDetail());
    alert.setAlertType(newAlert.getAlertType());
    return portalDashboardConfigService.update(alert);
  }

  @SandboxOnly
  @EnforcePortalEnvPermission(permission = "study_settings_edit")
  public ParticipantDashboardAlert createPortalEnvAlert(
          PortalEnvAuthContext authContext,
      AlertTrigger triggerName,
      ParticipantDashboardAlert newAlert) {

    if (!triggerName.equals(newAlert.getTrigger())) {
      throw new IllegalArgumentException(
          "Trigger name specified in payload does not match trigger name specified in path");
    }

    newAlert.setPortalEnvironmentId(authContext.getPortalEnvironment().getId());
    return portalDashboardConfigService.create(newAlert);
  }
}
