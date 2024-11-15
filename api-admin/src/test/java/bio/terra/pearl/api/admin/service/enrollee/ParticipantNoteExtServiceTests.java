package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ParticipantNoteExtServiceTests extends BaseSpringBootTest {
  @Autowired private ParticipantNoteExtService participantNoteExtService;

  @Test
  public void testAuthentication() {
    AuthTestUtils.assertAllMethodsAnnotated(
        participantNoteExtService,
        Map.of("create", AuthAnnotationSpec.withPortalEnrolleePerm("participant_data_edit")));
  }
}
