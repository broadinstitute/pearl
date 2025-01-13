package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.dashboard.ParticipantDashboardAlertDao;
import bio.terra.pearl.core.model.dashboard.AlertTrigger;
import bio.terra.pearl.core.model.dashboard.AlertType;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.publishing.ParticipantDashboardAlertChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.publishing.PortalEnvPublishable;
import bio.terra.pearl.core.service.publishing.PublishingUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class PortalDashboardConfigService extends CrudService<ParticipantDashboardAlert, ParticipantDashboardAlertDao>  implements PortalEnvPublishable {

    public PortalDashboardConfigService(ParticipantDashboardAlertDao participantDashboardAlertDao) {
        super(participantDashboardAlertDao);
    }

    public List<ParticipantDashboardAlert> findByPortalEnvId(UUID portalEnvId) {
        return dao.findByPortalEnvironmentId(portalEnvId);
    }

    public Optional<ParticipantDashboardAlert> findByPortalEnvIdAndTrigger(UUID portalEnvId, AlertTrigger trigger) {
        return dao.findByPortalEnvironmentIdAndTrigger(portalEnvId, trigger);
    }

    public ParticipantDashboardAlert update(ParticipantDashboardAlert participantDashboardAlert) {
        return dao.update(participantDashboardAlert);
    }

    public void deleteAlertsByPortalEnvId(UUID portalEnvId) {
        dao.deleteByPortalEnvironmentId(portalEnvId);
    }


    @Override
    public void loadForPublishing(PortalEnvironment portalEnv) {
        portalEnv.setParticipantDashboardAlerts(findByPortalEnvId(portalEnv.getId()));
    }

    @Override
    public void updateDiff(PortalEnvironmentChange change, PortalEnvironment sourceEnv, PortalEnvironment destEnv) {
        Map<AlertTrigger, ParticipantDashboardAlert> unmatchedDestAlerts = new HashMap<>();
        for (ParticipantDashboardAlert destAlert : destEnv.getParticipantDashboardAlerts()) {
            unmatchedDestAlerts.put(destAlert.getTrigger(), destAlert);
        }

        List<ParticipantDashboardAlertChange> alertChangeLists = new ArrayList<>();
        for (ParticipantDashboardAlert sourceAlert : sourceEnv.getParticipantDashboardAlerts()) {
            ParticipantDashboardAlert matchedAlert = unmatchedDestAlerts.get(sourceAlert.getTrigger());
            if (matchedAlert == null) {
                List<ConfigChange> newAlert = ConfigChange.allChanges(sourceAlert, null, getPublishIgnoreProps());
                alertChangeLists.add(new ParticipantDashboardAlertChange(sourceAlert.getTrigger(), newAlert));
            } else {
                unmatchedDestAlerts.remove(matchedAlert.getTrigger());
                List<ConfigChange> alertChanges = ConfigChange.allChanges(sourceAlert, matchedAlert, getPublishIgnoreProps());
                if(!alertChanges.isEmpty()) {
                    alertChangeLists.add(new ParticipantDashboardAlertChange(sourceAlert.getTrigger(), alertChanges));
                }
            }
        }
        change.setParticipantDashboardAlertChanges(alertChangeLists);
    }

    @Override
    public void applyDiff(PortalEnvironmentChange change, PortalEnvironment destEnv) {
        for (ParticipantDashboardAlertChange alertChanges : change.getParticipantDashboardAlertChanges()) {
            Optional<ParticipantDashboardAlert> destAlert = findByPortalEnvIdAndTrigger(destEnv.getId(), alertChanges.trigger());
            if (destAlert.isEmpty()) {
                // The alert doesn't exist in the dest env yet, so default all the required fields before
                // applying the changes from the change list
                ParticipantDashboardAlert newAlert = getDefaultDashboardAlert(destEnv, alertChanges.trigger());
                applyAlertChanges(newAlert, alertChanges.changes());
                create(newAlert);
            } else {
                ParticipantDashboardAlert alert = destAlert.get();
                applyAlertChanges(alert, alertChanges.changes());
                update(alert);
            }
        }
    }

    protected void applyAlertChanges(ParticipantDashboardAlert alert, List<ConfigChange> changes) {
        try {
            for (ConfigChange alertChange : changes) {
                PublishingUtils.setPropertyEnumSafe(alert, alertChange.propertyName(), alertChange.newValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error applying changes to alert: " + alert.getId(), e);
        }
    }


    private ParticipantDashboardAlert getDefaultDashboardAlert(PortalEnvironment destEnv, AlertTrigger trigger) {
        return ParticipantDashboardAlert.builder()
                .portalEnvironmentId(destEnv.getId())
                .alertType(AlertType.PRIMARY)
                .title("")
                .detail("")
                .portalEnvironmentId(destEnv.getId())
                .trigger(trigger)
                .build();
    }

}
