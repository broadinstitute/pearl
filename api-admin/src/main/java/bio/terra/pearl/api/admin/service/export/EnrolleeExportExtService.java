package bio.terra.pearl.api.admin.service.export;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.export.DictionaryExportService;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.export.ExportOptionsWithExpression;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import java.io.OutputStream;
import org.springframework.stereotype.Service;

@Service
public class EnrolleeExportExtService {
  private final EnrolleeExportService enrolleeExportService;
  private final DictionaryExportService dictionaryExportService;
  private final StudyEnvironmentConfigService studyEnvironmentConfigService;

  public EnrolleeExportExtService(
      EnrolleeExportService enrolleeExportService,
      DictionaryExportService dictionaryExportService,
      StudyEnvironmentConfigService studyEnvironmentConfigService) {
    this.enrolleeExportService = enrolleeExportService;
    this.dictionaryExportService = dictionaryExportService;
    this.studyEnvironmentConfigService = studyEnvironmentConfigService;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public void export(
      PortalStudyEnvAuthContext authContext, ExportOptionsWithExpression options, OutputStream os) {
    if (options.getTimeZone() == null) {
      StudyEnvironmentConfig studyEnvConfig =
          studyEnvironmentConfigService.findByStudyEnvironmentId(
              authContext.getStudyEnvironment().getId());
      options.setTimeZone(studyEnvConfig.getTimeZone());
    }
    enrolleeExportService.export(options, authContext.getStudyEnvironment().getId(), os);
  }

  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSION)
  public void exportDictionary(
      PortalStudyEnvAuthContext authContext, ExportOptions exportOptions, OutputStream os) {
    dictionaryExportService.exportDictionary(
        exportOptions,
        authContext.getPortal().getId(),
        authContext.getStudyEnvironment().getId(),
        os);
  }
}
