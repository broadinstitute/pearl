package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.kit.StudyEnvironmentKitTypeDao;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.ListChange;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.publishing.StudyEnvPublishable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StudyEnvironmentKitTypeService extends CrudService<StudyEnvironmentKitType, StudyEnvironmentKitTypeDao> implements StudyEnvPublishable {
    private final KitTypeDao kitTypeDao;

    public StudyEnvironmentKitTypeService(StudyEnvironmentKitTypeDao dao, KitTypeDao kitTypeDao) {
        super(dao);
        this.kitTypeDao = kitTypeDao;
    }

    public List<KitType> findKitTypesByStudyEnvironmentId(UUID studyEnvId) {
        List<StudyEnvironmentKitType> studyKitTypes = dao.findByStudyEnvironmentId(studyEnvId);
        return kitTypeDao.findAll(studyKitTypes.stream().map(StudyEnvironmentKitType::getKitTypeId).toList());
    }

    public List<KitType> findAllowedKitTypes() {
        return kitTypeDao.findAll();
    }

    public void deleteByKitTypeIdAndStudyEnvironmentId(UUID kitTypeId, UUID studyEnvId) {
        dao.deleteByKitTypeIdAndStudyEnvironmentId(kitTypeId, studyEnvId);
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId, Set<CascadeProperty> cascades) {
        for (StudyEnvironmentKitType studyEnvironmentKitType : dao.findByStudyEnvironmentId(studyEnvId)) {
            dao.delete(studyEnvironmentKitType.getId());
        }
    }

    @Override
    public void loadForPublishing(StudyEnvironment studyEnv) {
        List<KitType> kitTypes = findKitTypesByStudyEnvironmentId(studyEnv.getId());
        studyEnv.setKitTypes(kitTypes);
    }

    @Override
    public void updateDiff(StudyEnvironmentChange change, StudyEnvironment sourceEnv, StudyEnvironment destEnv) {
        List<KitType> unmatchedDestKitTypes = new ArrayList<>(destEnv.getKitTypes());
        List<KitType> addedKitTypes = new ArrayList<>();
        for (KitType sourceKitType : sourceEnv.getKitTypes()) {
            KitType matchedKitType = unmatchedDestKitTypes.stream().filter(
                            destKitType -> destKitType.getName().equals(sourceKitType.getName()))
                    .findAny().orElse(null);
            if (matchedKitType == null) {
                addedKitTypes.add(sourceKitType);
            } else {
                unmatchedDestKitTypes.remove(matchedKitType);
            }
        }
        change.setKitTypeChanges(new ListChange<>(addedKitTypes, unmatchedDestKitTypes, Collections.emptyList()));
    }

    @Override
    @Transactional
    public void applyDiff(StudyEnvironmentChange change, StudyEnvironment destEnv, PortalEnvironment destPortalEnv) {
        for (KitType kitType : change.getKitTypeChanges().addedItems()) {
            StudyEnvironmentKitType studyEnvKitType = new StudyEnvironmentKitType();
            studyEnvKitType.setKitTypeId(kitType.getId());
            studyEnvKitType.setStudyEnvironmentId(destEnv.getId());

            create(studyEnvKitType);
        }
        for (KitType kitType : change.getKitTypeChanges().removedItems()) {
            deleteByKitTypeIdAndStudyEnvironmentId(kitType.getId(), destEnv.getId());
        }
        // we really don't have a concept of "changed" kit types, so we don't need to do anything here
    }
}
