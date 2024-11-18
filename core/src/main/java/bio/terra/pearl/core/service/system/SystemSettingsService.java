package bio.terra.pearl.core.service.system;

import bio.terra.juniper.core.model.maintenance.SystemSettings;
import bio.terra.pearl.core.dao.system.SystemSettingsDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SystemSettingsService {

    private final SystemSettingsDao systemSettingsDao;

    public SystemSettingsService(SystemSettingsDao systemSettingsDao) {
        this.systemSettingsDao = systemSettingsDao;
    }

    public SystemSettings getSystemSettings() {
        //system_settings is a singleton table
        // if we have multiple rows, we'll log it and return the most recent row to avoid mass-disruption
        List<SystemSettings> settings = systemSettingsDao.findAll();
        if (settings.size() > 1) {
            log.error("Multiple system settings rows found, returning the most recent");
            return settings.getLast();
        }

        if(settings.isEmpty()) {
            log.info("No system settings found, returning default settings");
            return SystemSettings.builder()
                    .maintenanceModeBypassPhrase(null)
                    .maintenanceModeEnabled(false)
                    .disableScheduledJobs(false)
                    .maintenanceModeMessage(null).build();
        }

        return settings.getFirst();
    }

    public SystemSettings updateMaintenanceModeSettings(SystemSettings systemSettings) {
        systemSettingsDao.update(systemSettings);
        return systemSettings;
    }

}
