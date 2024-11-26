package bio.terra.pearl.api.admin.service.portal;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AnyAdminUser;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PortalExtServiceTest extends BaseSpringBootTest {
  @Autowired private PortalExtService portalExtService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        portalExtService,
        Map.of(
            "fullLoad",
            AuthAnnotationSpec.withPortalPerm(AuthUtilService.BASE_PERMISSION),
            "getAll",
            AuthAnnotationSpec.withOtherAnnotations(List.of(AnyAdminUser.class)),
            "updateConfig",
            AuthAnnotationSpec.withPortalEnvPerm("study_settings_edit"),
            "updateEnvironment",
            AuthAnnotationSpec.withPortalEnvPerm("study_settings_edit"),
            "setLanguages",
            AuthAnnotationSpec.withPortalEnvPerm("study_settings_edit")));
  }
}
