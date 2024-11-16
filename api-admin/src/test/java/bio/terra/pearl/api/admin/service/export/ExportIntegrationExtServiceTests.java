package bio.terra.pearl.api.admin.service.export;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ExportIntegrationExtServiceTests extends BaseSpringBootTest {

  @Autowired private ExportIntegrationExtService exportIntegrationExtService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        exportIntegrationExtService,
        Map.of(
            "list",
            AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSION),
            "find",
            AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSION),
            "run",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view"),
            "create",
            AuthAnnotationSpec.withPortalStudyEnvPerm("export_integration"),
            "save",
            AuthAnnotationSpec.withPortalStudyEnvPerm("export_integration"),
            "listJobs",
            AuthAnnotationSpec.withPortalStudyEnvPerm(AuthUtilService.BASE_PERMISSION)));
  }
}
