package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.study.*;
import bio.terra.pearl.core.service.study.exception.StudyEnvConfigMissing;
import bio.terra.pearl.populate.dto.StudyEnvironmentPopDto;
import bio.terra.pearl.populate.dto.StudyPopDto;
import bio.terra.pearl.populate.dto.notifications.TriggerPopDto;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyExtractor {
    private final ObjectMapper objectMapper;
    private final StudyService studyService;
    private final PortalStudyService portalStudyService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final StudyEnvironmentConfigService studyEnvironmentConfigService;
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final TriggerService triggerService;
    private final StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;

    public StudyExtractor(@Qualifier("extractionObjectMapper") ObjectMapper objectMapper, StudyService studyService,
                          PortalStudyService portalStudyService, StudyEnvironmentService studyEnvironmentService,
                          StudyEnvironmentConfigService studyEnvironmentConfigService, StudyEnvironmentSurveyService studyEnvironmentSurveyService,
                          TriggerService triggerService, StudyEnvironmentKitTypeService studyEnvironmentKitTypeService) {
        this.studyService = studyService;
        this.portalStudyService = portalStudyService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.objectMapper = objectMapper;
        this.triggerService = triggerService;
        this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
        objectMapper.addMixIn(Study.class, StudyMixin.class);
        objectMapper.addMixIn(StudyEnvironment.class, StudyEnvironmentMixin.class);
    }

    public void writeStudies(Portal portal, ExtractPopulateContext context) {
        List<PortalStudy> portalStudyList = portalStudyService.findByPortalId(portal.getId());
        for (PortalStudy portalStudy : portalStudyList) {
            writeStudy(portalStudy, context);
        }
    }

    public void writeStudy(PortalStudy portalStudy, ExtractPopulateContext context) {
        Study study = studyService.find(portalStudy.getStudyId()).orElseThrow();
        String studyFileName = "studies/%s/study.json".formatted(study.getShortcode());
        StudyPopDto studyPopDto = new StudyPopDto();
        studyPopDto.setShortcode(study.getShortcode());
        studyPopDto.setName(study.getName());
        List<StudyEnvironment> studyEnvs = studyEnvironmentService.findByStudy(study.getId());
        for (StudyEnvironment studyEnv : studyEnvs) {
            StudyEnvironmentPopDto studyEnvPopDto = extractStudyEnv(studyEnv, studyPopDto, context);
            studyPopDto.getStudyEnvironmentDtos().add(studyEnvPopDto);
        }
        try {
            String studyString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(studyPopDto);
            context.writeFileForEntity(studyFileName, studyString, study.getId());
            context.getPortalPopDto().getPopulateStudyFiles().add(studyFileName);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error writing study %s to json".formatted(study.getShortcode()), e);
        }
    }

    public StudyEnvironmentPopDto extractStudyEnv(StudyEnvironment studyEnv,
                                                  StudyPopDto studyPopDto,
                                                  ExtractPopulateContext context) {
        StudyEnvironmentPopDto studyEnvPopDto = new StudyEnvironmentPopDto();
        studyEnvPopDto.setEnvironmentName(studyEnv.getEnvironmentName());
        studyEnvPopDto.setStudyEnvironmentConfig(
                studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId()).orElseThrow(StudyEnvConfigMissing::new)
        );
        if (studyEnv.getPreEnrollSurveyId() != null) {
            SurveyExtractor.SurveyPopDtoStub surveyPopDtoStub = new SurveyExtractor.SurveyPopDtoStub();
            surveyPopDtoStub.setPopulateFileName("../../" + context.getFileNameForEntity(studyEnv.getPreEnrollSurveyId()));
            studyEnvPopDto.setPreEnrollSurveyDto(surveyPopDtoStub);
        }
        List<StudyEnvironmentSurvey> studyEnvSurveys = studyEnvironmentSurveyService.findAllByStudyEnvId(studyEnv.getId(), context.isExtractActiveVersionsOnly() ? true : null);
        for (StudyEnvironmentSurvey studyEnvSurvey : studyEnvSurveys) {;
            StudyEnvironmentSurveyPopDto studyEnvSurveyPopDto = new StudyEnvironmentSurveyPopDto();
            BeanUtils.copyProperties(studyEnvSurvey, studyEnvSurveyPopDto, "id", "studyEnvironmentId", "surveyId");
            studyEnvSurveyPopDto.setPopulateFileName("../../" + context.getFileNameForEntity(studyEnvSurvey.getSurveyId()));
            studyEnvPopDto.getConfiguredSurveyDtos().add(studyEnvSurveyPopDto);
        }

        List<Trigger> triggers = triggerService.findByStudyEnvironmentId(studyEnv.getId());
        for (Trigger config : triggers) {;
            TriggerPopDto configPopDto = new TriggerPopDto();
            BeanUtils.copyProperties(config, configPopDto, "id", "studyEnvironmentId", "portalEnvironmentId", "emailTemplateId");
            String filename = "../../" + context.getFileNameForEntity(config.getEmailTemplateId());
            configPopDto.setPopulateFileName(filename);
            studyEnvPopDto.getTriggerDtos().add(configPopDto);
        }

        List<KitType> kitTypes = studyEnvironmentKitTypeService.findKitTypesByStudyEnvironmentId(studyEnv.getId());
        studyEnvPopDto.setKitTypeNames(kitTypes.stream().map(KitType::getName).toList());
        return studyEnvPopDto;
    }



    protected static class StudyMixin {
        @JsonIgnore
        public List<StudyEnvironment> getStudyEnvironments() { return null; }
    }
    protected static class StudyEnvironmentMixin {
        @JsonIgnore
        public List<StudyEnvironmentSurvey> getStudyEnvironmentSurveys() { return null; }
        @JsonIgnore
        public List<Trigger> getNotificationConfigs() { return null; }
        @JsonIgnore
        public List<StudyEnvironmentSurvey> getConfiguredSurveys() { return null; }
    }
}
