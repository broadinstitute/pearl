package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.study.StudyEnvironment;

public interface StudyEnvPublishable {
    void loadForDiffing(StudyEnvironment studyEnv);
    void updateDiff(StudyEnvironmentChange change, StudyEnvironment sourceEnv, StudyEnvironment destEnv);
    void applyDiff( StudyEnvironmentChange change, StudyEnvironment destEnv, PortalEnvironment destPortalEnv);


}
