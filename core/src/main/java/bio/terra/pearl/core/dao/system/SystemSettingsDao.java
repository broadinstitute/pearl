package bio.terra.pearl.core.dao.system;

import bio.terra.juniper.core.model.maintenance.SystemSettings;
import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class SystemSettingsDao extends BaseMutableJdbiDao<SystemSettings> {

    public SystemSettingsDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<SystemSettings> getClazz() {
        return SystemSettings.class;
    }
}
