package bio.terra.pearl.api.admin.service.system;

import bio.terra.juniper.core.model.maintenance.SystemSettings;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.core.service.system.SystemSettingsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingsExtService {

  private final SystemSettingsService systemSettingsService;

  public SystemSettingsExtService(SystemSettingsService systemSettingsService) {
    this.systemSettingsService = systemSettingsService;
  }

  @SuperuserOnly
  public SystemSettings updateSystemSettings(
      OperatorAuthContext authContext, SystemSettings updatedSystemSettings) {
    SystemSettings currentSystemSettings = systemSettingsService.getSystemSettings();
    BeanUtils.copyProperties(updatedSystemSettings, currentSystemSettings);

    return systemSettingsService.updateSystemSettings(currentSystemSettings);
  }
}
