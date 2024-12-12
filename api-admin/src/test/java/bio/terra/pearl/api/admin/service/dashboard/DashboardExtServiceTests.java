package bio.terra.pearl.api.admin.service.dashboard;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.SandboxOnly;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DashboardExtServiceTests extends BaseSpringBootTest {

  @Autowired private DashboardExtService dashboardExtService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        dashboardExtService,
        Map.of(
            "listPortalEnvAlerts",
            AuthAnnotationSpec.withPortalEnvPerm(AuthUtilService.BASE_PERMISSION),
            "updatePortalEnvAlert",
            AuthAnnotationSpec.withPortalEnvPerm("study_settings_edit", List.of(SandboxOnly.class)),
            "createPortalEnvAlert",
            AuthAnnotationSpec.withPortalEnvPerm(
                "study_settings_edit", List.of(SandboxOnly.class))));
  }
}
