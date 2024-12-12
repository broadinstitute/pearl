package bio.terra.pearl.api.admin.service.study;

import bio.terra.pearl.api.admin.models.dto.StudyCreationDto;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalPermission;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyPermission;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.populate.dto.StudyPopDto;
import bio.terra.pearl.populate.service.FilePopulateService;
import bio.terra.pearl.populate.service.StudyPopulator;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyExtService {
  private final StudyService studyService;
  private final PortalStudyService portalStudyService;
  private final StudyPopulator studyPopulator;
  private final FilePopulateService filePopulateService;

  public StudyExtService(
      StudyService studyService,
      PortalStudyService portalStudyService,
      StudyPopulator studyPopulator,
      FilePopulateService filePopulateService) {
    this.studyService = studyService;
    this.portalStudyService = portalStudyService;
    this.studyPopulator = studyPopulator;
    this.filePopulateService = filePopulateService;
  }

  @Transactional
  @SuperuserOnly
  @EnforcePortalPermission(permission = AuthUtilService.BASE_PERMISSION)
  public Study create(PortalAuthContext authContext, StudyCreationDto study) {
    /** Create empty environments for each of sandbox, irb, and live */
    List<StudyEnvironment> studyEnvironments =
        Arrays.stream(EnvironmentName.values())
            .map(envName -> makeEmptyEnvironment(envName, envName == EnvironmentName.sandbox))
            .toList();

    Study newStudy =
        Study.builder()
            .shortcode(study.getShortcode())
            .name(study.getName())
            .studyEnvironments(studyEnvironments)
            .build();
    newStudy = studyService.create(newStudy);
    portalStudyService.create(authContext.getPortal().getId(), newStudy.getId());

    if (Objects.nonNull(study.getTemplate())) {
      fillInWithTemplate(authContext.getPortalShortcode(), newStudy, study.getTemplate());
    }

    return newStudy;
  }

  /** gets all the studies for a portal, with the environments attached */
  @EnforcePortalPermission(permission = AuthUtilService.BASE_PERMISSION)
  public List<Study> getStudiesWithEnvs(PortalAuthContext authContext, EnvironmentName envName) {
    List<Study> studies = studyService.findByPortalId(authContext.getPortal().getId());
    studies.forEach(
        study -> {
          studyService.attachEnvironments(study);
          study.setStudyEnvironments(
              study.getStudyEnvironments().stream()
                  .filter(env -> envName == null || env.getEnvironmentName().equals(envName))
                  .toList());
        });
    return studies;
  }

  @Transactional
  @SuperuserOnly
  @EnforcePortalStudyPermission(permission = AuthUtilService.BASE_PERMISSION)
  public void delete(PortalStudyAuthContext authContext) {
    portalStudyService.deleteByStudyId(authContext.getPortalStudy().getStudyId());
    studyService.delete(authContext.getPortalStudy().getStudyId(), CascadeProperty.EMPTY_SET);
  }

  private void fillInWithTemplate(
      String portalShortcode, Study newStudy, StudyCreationDto.StudyTemplate studyTemplate) {
    String filename;

    switch (studyTemplate) {
      default -> {
        filename = "basic_study.json";
      }
    }

    PortalPopulateContext config =
        new PortalPopulateContext(
            "templates/" + filename, portalShortcode, null, new HashMap<>(), false, null);

    StudyPopDto studyPopDto;

    try {
      String fileContents = filePopulateService.readFile(filename, config);
      studyPopDto = studyPopulator.readValue(fileContents);

      studyPopDto.setShortcode(newStudy.getShortcode());
      studyPopDto.setName(newStudy.getName());

      studyPopulator.populateFromDto(studyPopDto, config, false);
    } catch (IOException e) {
      throw new InternalServerException("Failed to pre-populate study.", e);
    }
  }

  /**
   * we make empty environments as placeholders for the environment views. This minimizes the amount
   * of hardcoding we have to do in the UI around sandbox/irb/prod, giving us the flexibility to add
   * more alternate environments in the future
   */
  private StudyEnvironment makeEmptyEnvironment(EnvironmentName envName, boolean initialized) {
    StudyEnvironment studyEnv =
        StudyEnvironment.builder()
            .environmentName(envName)
            .studyEnvironmentConfig(
                StudyEnvironmentConfig.builder().initialized(initialized).build())
            .build();
    return studyEnv;
  }
}
