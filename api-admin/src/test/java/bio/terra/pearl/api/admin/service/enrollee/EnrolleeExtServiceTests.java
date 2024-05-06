package bio.terra.pearl.api.admin.service.enrollee;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class EnrolleeExtServiceTests extends BaseSpringBootTest {
  @Autowired private StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired private EnrolleeFactory enrolleeFactory;
  @Autowired private EnrolleeExtService enrolleeExtService;
  @Autowired private PortalAdminUserFactory portalAdminUserFactory;

  @Test
  @Transactional
  public void testFindById(TestInfo info) {
    StudyEnvironmentFactory.StudyEnvironmentBundle bundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    AdminUser operator =
        portalAdminUserFactory
            .buildPersistedWithPortals(getTestName(info), List.of(bundle.getPortal()))
            .user();
    EnrolleeFactory.EnrolleeBundle enrollee1 =
        enrolleeFactory.buildWithPortalUser(
            getTestName(info), bundle.getPortalEnv(), bundle.getStudyEnv());

    Enrollee loadedEnrollee =
        enrolleeExtService.findWithAdminLoad(operator, enrollee1.enrollee().getShortcode());
    assertThat(loadedEnrollee.getId(), equalTo(enrollee1.enrollee().getId()));

    loadedEnrollee =
        enrolleeExtService.findWithAdminLoad(operator, enrollee1.enrollee().getId().toString());
    assertThat(loadedEnrollee.getId(), equalTo(enrollee1.enrollee().getId()));

    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          enrolleeExtService.findWithAdminLoad(operator, UUID.randomUUID().toString());
        });

    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          enrolleeExtService.findWithAdminLoad(operator, "BADCODE");
        });
  }
}