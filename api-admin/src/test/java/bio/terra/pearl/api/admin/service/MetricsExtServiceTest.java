package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MetricsExtServiceTest extends BaseSpringBootTest {

  @Autowired MetricsExtService metricsExtService;

  @Test
  public void testAllMethodsAnnotated() {
    AuthTestUtils.assertAllMethodsAnnotated(
        metricsExtService,
        Map.of(
            "loadMetrics",
            AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSION)));
  }
}
