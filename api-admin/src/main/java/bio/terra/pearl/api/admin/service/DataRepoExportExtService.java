package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.datarepo.DataRepoJob;
import bio.terra.pearl.core.model.datarepo.Dataset;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DataRepoExportExtService {

  private AuthUtilService authUtilService;
  private DataRepoExportService dataRepoExportService;
  private StudyEnvironmentService studyEnvironmentService;

  public DataRepoExportExtService(
      AuthUtilService authUtilService,
      DataRepoExportService dataRepoExportService,
      StudyEnvironmentService studyEnvironmentService) {
    this.authUtilService = authUtilService;
    this.dataRepoExportService = dataRepoExportService;
    this.studyEnvironmentService = studyEnvironmentService;
  }

  public List<Dataset> getDatasetsForStudyEnvironment(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get(); // todo get

    return dataRepoExportService.getDatasetsForStudyEnvironment(studyEnv.getId());
  }

  public List<DataRepoJob> getJobHistoryForDataset(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String datasetName,
      AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode); // ??
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get(); // todo get

    return dataRepoExportService.getJobHistoryForDataset(studyEnv.getId(), datasetName);
  }

  public String createDataset(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      AdminUser user) {
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode); // ??
    authUtilService.authUserToStudy(user, portalShortcode, studyShortcode);

    StudyEnvironment studyEnv =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get(); // todo get

    return dataRepoExportService.createDataset(
        studyEnv,
        "d2p_mbemis_"
            + System.currentTimeMillis()
            + "_"
            + studyShortcode
            + "_"
            + environmentName.name());
  }
}
