package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.dao.metrics.MetricName;
import bio.terra.pearl.core.dao.metrics.MetricsDao;
import bio.terra.pearl.core.model.metrics.BasicMetricDatum;
import bio.terra.pearl.core.model.metrics.TimeRange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MetricsExtService {
  private MetricsDao metricsDao;

  public MetricsExtService(MetricsDao metricsDao) {
    this.metricsDao = metricsDao;
  }

  @EnforcePortalStudyEnvPermission(permission = AuthUtilService.BASE_PERMISSION)
  public List<BasicMetricDatum> loadMetrics(
      PortalStudyEnvAuthContext authContext, MetricName metricName) {
    StudyEnvironment studyEnv = authContext.getStudyEnvironment();
    if (MetricName.STUDY_ENROLLMENT.equals(metricName)) {
      return metricsDao.studyEnrollments(studyEnv.getId(), new TimeRange(null, null));
    } else if (MetricName.STUDY_ENROLLEE_CONSENTED.equals(metricName)) {
      return metricsDao.studyConsentedEnrollees(studyEnv.getId(), new TimeRange(null, null));
    } else if (MetricName.STUDY_SURVEY_COMPLETION.equals(metricName)) {
      return metricsDao.studySurveyCompletions(studyEnv.getId(), new TimeRange(null, null));
    } else if (MetricName.STUDY_REQUIRED_SURVEY_COMPLETION.equals(metricName)) {
      return metricsDao.studyRequiredSurveyCompletions(studyEnv.getId(), new TimeRange(null, null));
    }
    throw new IllegalArgumentException("Unrecognized metric name '" + metricName + "'");
  }
}
