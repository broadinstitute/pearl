package bio.terra.pearl.api.admin.controller.dashboard;

import bio.terra.pearl.api.admin.api.DashboardApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.api.admin.service.dashboard.DashboardExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dashboard.AlertTrigger;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardController implements DashboardApi {

  private HttpServletRequest request;
  private AuthUtilService authUtilService;
  private DashboardExtService dashboardExtService;
  private ObjectMapper objectMapper;

  public DashboardController(
      HttpServletRequest request,
      AuthUtilService authUtilService,
      DashboardExtService dashboardExtService,
      ObjectMapper objectMapper) {
    this.request = request;
    this.authUtilService = authUtilService;
    this.dashboardExtService = dashboardExtService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> listPortalEnvAlerts(String portalShortcode, String envName) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    List<ParticipantDashboardAlert> alerts =
        dashboardExtService.listPortalEnvAlerts(
            PortalEnvAuthContext.of(operator, portalShortcode, environmentName));

    return ResponseEntity.ok(alerts);
  }

  @Override
  public ResponseEntity<Object> updatePortalEnvAlert(
      String portalShortcode, String envName, String triggerName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    ParticipantDashboardAlert alert =
        objectMapper.convertValue(body, ParticipantDashboardAlert.class);

    AlertTrigger trigger = AlertTrigger.valueOf(triggerName);

    ParticipantDashboardAlert updatedAlert =
        dashboardExtService.updatePortalEnvAlert(
            PortalEnvAuthContext.of(operator, portalShortcode, environmentName), trigger, alert);

    return ResponseEntity.ok(updatedAlert);
  }

  @Override
  public ResponseEntity<Object> createPortalEnvAlert(
      String portalShortcode, String envName, String triggerName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);

    ParticipantDashboardAlert alert =
        objectMapper.convertValue(body, ParticipantDashboardAlert.class);

    AlertTrigger trigger = AlertTrigger.valueOf(triggerName);

    ParticipantDashboardAlert createdAlert =
        dashboardExtService.createPortalEnvAlert(
            PortalEnvAuthContext.of(operator, portalShortcode, environmentName), trigger, alert);

    return ResponseEntity.ok(createdAlert);
  }
}
