package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.dao.publishing.PortalEnvironmentChangeRecordDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.dashboard.ParticipantDashboardAlert;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChangeRecord;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.portal.PortalDashboardConfigService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * dedicated service for applying deltas to portal environments
 */
@Service
public class PortalPublishingService {
    private final StudyService studyService;
    private final PortalEnvironmentService portalEnvService;
    private final PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao;
    private final PortalDashboardConfigService portalDashboardConfigService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final List<PortalEnvPublishable> portalEnvPublishables;
    private final List<StudyEnvPublishable> studyEnvPublishables;
    private final ObjectMapper objectMapper;


    public PortalPublishingService(StudyService studyService, PortalEnvironmentService portalEnvService, PortalEnvironmentChangeRecordDao portalEnvironmentChangeRecordDao,
                                   PortalDashboardConfigService portalDashboardConfigService,
                                   StudyEnvironmentService studyEnvironmentService,
                                   List<PortalEnvPublishable> portalEnvPublishables,
                                   List<StudyEnvPublishable> studyEnvPublishables,
                                   ObjectMapper objectMapper) {
        this.studyService = studyService;
        this.portalEnvService = portalEnvService;
        this.portalEnvironmentChangeRecordDao = portalEnvironmentChangeRecordDao;
        this.portalDashboardConfigService = portalDashboardConfigService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.portalEnvPublishables = portalEnvPublishables;
        this.studyEnvPublishables = studyEnvPublishables;
        this.objectMapper = objectMapper;
    }


    public PortalEnvironmentChange diffPortalEnvs(String shortcode, EnvironmentName source, EnvironmentName dest) {
        PortalEnvironment sourceEnv = loadPortalEnvForProcessing(shortcode, source);
        PortalEnvironment destEnv = loadPortalEnvForProcessing(shortcode, dest);
        return diffPortalEnvs(sourceEnv, destEnv);
    }

    public PortalEnvironmentChange diffPortalEnvs(PortalEnvironment sourceEnv, PortalEnvironment destEnv) {
        List<StudyEnvironmentChange> studyEnvChanges = new ArrayList<>();
        List<Study> studies = studyService.findByPortalId(sourceEnv.getPortalId());
        for (Study study : studies) {
            StudyEnvironmentChange studyEnvChange = diffStudyEnvs(study.getShortcode(), sourceEnv.getEnvironmentName(), destEnv.getEnvironmentName());
            studyEnvChanges.add(studyEnvChange);
        }
        PortalEnvironmentChange change = PortalEnvironmentChange.builder()
                .studyEnvChanges(studyEnvChanges)
                .build();
        for(PortalEnvPublishable publishable : portalEnvPublishables) {
            publishable.updateDiff(change, sourceEnv, destEnv);
        }

        return change;
    }


    public StudyEnvironmentChange diffStudyEnvs(String studyShortcode, EnvironmentName source, EnvironmentName dest) {
        StudyEnvironment sourceEnv = loadStudyEnvForProcessing(studyShortcode, source);
        StudyEnvironment destEnv = loadStudyEnvForProcessing(studyShortcode, dest);
        return diffStudyEnvs(studyShortcode, sourceEnv, destEnv);
    }

    /**
     * updates the dest environment with the given changes
     */
    @Transactional
    public PortalEnvironment applyChanges(String portalShortcode, EnvironmentName dest, PortalEnvironmentChange change, AdminUser operator) {
        PortalEnvironment destEnv = loadPortalEnvForProcessing(portalShortcode, dest);
        return applyChanges(destEnv, change, operator);
    }


    protected PortalEnvironment loadPortalEnvForProcessing(String shortcode, EnvironmentName envName) {
        PortalEnvironment portalEnv = portalEnvService.findOne(shortcode, envName).get();
        for (PortalEnvPublishable publishable : portalEnvPublishables) {
            publishable.loadForPublishing(portalEnv);
        }
        return portalEnv;
    }


    /**
     * applies the given update -- the destEnv provided must already be fully-hydrated from loadPortalEnv
     * returns the updated environment
     */
    protected PortalEnvironment applyChanges(PortalEnvironment destEnv, PortalEnvironmentChange envChanges, AdminUser operator) {
        for (PortalEnvPublishable portalEnvPublishable : portalEnvPublishables) {
            portalEnvPublishable.applyDiff(envChanges, destEnv);
        }

        for (StudyEnvironmentChange studyEnvChange : envChanges.getStudyEnvChanges()) {
            StudyEnvironment studyEnv = loadStudyEnvForProcessing(studyEnvChange.getStudyShortcode(), destEnv.getEnvironmentName());
            applyChanges(studyEnv, studyEnvChange, destEnv);
        }
        try {
            PortalEnvironmentChangeRecord changeRecord = PortalEnvironmentChangeRecord.builder()
                    .adminUserId(operator.getId())
                    .portalId(destEnv.getPortalId())
                    .environmentName(destEnv.getEnvironmentName())
                    .portalEnvironmentChange(objectMapper.writeValueAsString(envChanges))
                    .build();
            portalEnvironmentChangeRecordDao.create(changeRecord);
        } catch (Exception e) {
            throw new InternalServerException("error writing publish audit log", e);
        }
        return destEnv;
    }

    public StudyEnvironment loadStudyEnvForProcessing(String shortcode, EnvironmentName envName) {
        StudyEnvironment studyEnvironment = studyEnvironmentService.findByStudy(shortcode, envName).get();
        for (StudyEnvPublishable publishable : studyEnvPublishables) {
            publishable.loadForPublishing(studyEnvironment);
        }
        return studyEnvironment;
    }

    /** assumes the source and dest environments are fully loaded */
    public StudyEnvironmentChange diffStudyEnvs(String studyShortcode, StudyEnvironment sourceEnv, StudyEnvironment destEnv) {
        StudyEnvironmentChange change = StudyEnvironmentChange.builder()
                .studyShortcode(studyShortcode).build();
        for (StudyEnvPublishable publishable : studyEnvPublishables) {
            publishable.updateDiff(change, sourceEnv, destEnv);
        }
        return change;
    }

    /** assumes the dest environment is fully loaded */
    public void applyChanges(StudyEnvironment destEnv, StudyEnvironmentChange envChange,
                                         PortalEnvironment destPortalEnv) {
        for (StudyEnvPublishable publishable : studyEnvPublishables) {
            publishable.applyDiff(envChange, destEnv, destPortalEnv);
        }
    }

}
