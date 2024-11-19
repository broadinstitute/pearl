package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EnrolleeSearchExtServiceTests extends BaseSpringBootTest {
  @Autowired private EnrolleeSearchExtService enrolleeSearchExtService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        enrolleeSearchExtService,
        Map.of(
            "getExpressionSearchFacets",
            AuthAnnotationSpec.withPortalStudyEnvPerm("BASE"),
            "executeSearchExpression",
            AuthAnnotationSpec.withPortalStudyEnvPerm("participant_data_view")));
  }
}
