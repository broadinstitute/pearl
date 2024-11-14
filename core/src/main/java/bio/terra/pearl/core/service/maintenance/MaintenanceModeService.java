package bio.terra.pearl.core.service.maintenance;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MaintenanceModeService {

    //TODO: hook this up to the DAO

    public MaintenanceModeService() { }

    public Map<String, Object> getMaintenanceModeSettings() {
        Map<String, Object> config = new HashMap<>();
        config.put(
            "message", "This website is currently undergoing scheduled maintenance. "
                    + "All study activities will be unavailable during this time. "
                    + "We expect to be back online by **9:00 PM EST** on **12/11/2024**. "
                    + "Please contact [support@juniper.terra.bio](mailto:support@juniper.terra.bio) "
                    + "if you have any questions or need additional support.");
        config.put("bypassPhrase", "broad_institute");
        config.put("enabled", true);
        config.put("disableScheduledJobs", false);

        return config;
    }

}
