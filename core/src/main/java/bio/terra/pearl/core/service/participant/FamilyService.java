package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.dao.participant.FamilyDao;
import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Family;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FamilyService extends DataAuditedService<Family, FamilyDao> {
    private final ShortcodeService shortcodeService;
    private final EnrolleeDao enrolleeDao;
    private final EnrolleeRelationDao enrolleeRelationDao;
    private final ProfileDao profileDao;
    private final FamilyEnrolleeService familyEnrolleeService;

    public FamilyService(FamilyDao familyDao,
                         DataChangeRecordService dataChangeRecordService,
                         ObjectMapper objectMapper,
                         ShortcodeService shortcodeService,
                         EnrolleeDao enrolleeDao,
                         EnrolleeRelationDao enrolleeRelationDao,
                         ProfileDao profileDao,
                         FamilyEnrolleeService familyEnrolleeService) {
        super(familyDao, dataChangeRecordService, objectMapper);
        this.shortcodeService = shortcodeService;
        this.enrolleeDao = enrolleeDao;
        this.enrolleeRelationDao = enrolleeRelationDao;
        this.profileDao = profileDao;
        this.familyEnrolleeService = familyEnrolleeService;
    }

    @Transactional
    public Family create(Family family, DataAuditInfo info) {
        if (family.getShortcode() == null) {
            family.setShortcode(shortcodeService.generateShortcode("F", dao::findOneByShortcode));
        }
        return super.create(family, info);
    }

    public Optional<Family> findOneByShortcode(String shortcode) {
        return dao.findOneByShortcode(shortcode);
    }

    public Family loadForAdminView(Family family) {
        family.setMembers(enrolleeDao.findAllByFamilyId(family.getId()).stream().map(enrollee -> {
            enrollee.setProfile(profileDao.find(enrollee.getProfileId()).orElse(null));
            return enrollee;
        }).toList());
        family.setRelations(enrolleeRelationDao.findRelationsForFamily(family.getId()));
        family.setProband(enrolleeDao.find(family.getProbandEnrolleeId()).map(e -> {
            e.setProfile(profileDao.find(e.getProfileId()).orElse(null));
            return e;
        }).orElse(null));
        return family;
    }

    public List<Family> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return dao.findByStudyEnvironmentId(studyEnvironmentId);
    }

    @Override
    @Transactional
    public void delete(UUID familyId, DataAuditInfo info) {
        familyEnrolleeService.deleteByFamilyId(familyId, info);

        super.delete(familyId, info);
    }


    public List<Family> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    @Transactional
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId, DataAuditInfo info) {
        List<Family> families = findByStudyEnvironmentId(studyEnvironmentId);
        bulkDelete(families, info);
    }
}
