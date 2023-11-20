package bio.terra.pearl.core.dao.dashboard;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.ParticipantDashboardAlert;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ParticipantDashboardAlertDao extends BaseMutableJdbiDao<ParticipantDashboardAlert> {

    public ParticipantDashboardAlertDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ParticipantDashboardAlert> getClazz() {
        return ParticipantDashboardAlert.class;
    }

    public List<ParticipantDashboardAlert> findByPortalEnvironmentId(UUID portalEnvironmentId) {
        return findAllByProperty("portal_environment_id", portalEnvironmentId);
    }

}
