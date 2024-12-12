package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SurveyResponseExtServiceTests extends BaseSpringBootTest {
  @Autowired private SurveyResponseExtService surveyResponseExtService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        surveyResponseExtService,
        Map.of(
            "updateResponse", AuthAnnotationSpec.withPortalEnrolleePerm("participant_data_edit")));
  }
}
