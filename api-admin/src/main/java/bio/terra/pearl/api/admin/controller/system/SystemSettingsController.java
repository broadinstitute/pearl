package bio.terra.pearl.api.admin.controller.system;

import bio.terra.juniper.core.model.maintenance.SystemSettings;
import bio.terra.pearl.api.admin.api.SystemSettingsApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.system.SystemSettingsExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class SystemSettingsController implements SystemSettingsApi {

  private final SystemSettingsExtService systemSettingsExtService;
  private final ObjectMapper objectMapper;
  private final HttpServletRequest request;
  private final AuthUtilService authUtilService;

  public SystemSettingsController(
      SystemSettingsExtService systemSettingsExtService,
      ObjectMapper objectMapper,
      HttpServletRequest request,
      AuthUtilService authUtilService) {
    this.systemSettingsExtService = systemSettingsExtService;
    this.objectMapper = objectMapper;
    this.request = request;
    this.authUtilService = authUtilService;
  }

  @Override
  public ResponseEntity<Object> update(Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    SystemSettings settingsUpdate = objectMapper.convertValue(body, SystemSettings.class);

    SystemSettings response =
        systemSettingsExtService.updateSystemSettings(
            OperatorAuthContext.of(operator), settingsUpdate);
    return ResponseEntity.ok(response);
  }
}
