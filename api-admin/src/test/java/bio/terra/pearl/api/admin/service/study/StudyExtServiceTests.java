package bio.terra.pearl.api.admin.service.study;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.models.dto.StudyCreationDto;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.SuperuserOnly;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyAuthContext;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.factory.admin.AdminUserBundle;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.admin.PortalAdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.populate.service.BaseSeedPopulator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class StudyExtServiceTests extends BaseSpringBootTest {
  @Autowired private StudyExtService studyExtService;
  @Autowired private PortalFactory portalFactory;
  @Autowired private PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private PortalAdminUserFactory portalAdminUserFactory;
  @Autowired private StudyEnvironmentService studyEnvironmentService;
  @Autowired private StudyService studyService;
  @Autowired private PortalStudyService portalStudyService;
  @Autowired private TriggerService triggerService;
  @Autowired private StudyFactory studyFactory;
  @Autowired private BaseSeedPopulator baseSeedPopulator;

  @Test
  public void allMethodsAuthed(TestInfo info) {
    AuthTestUtils.assertAllMethodsAnnotated(
        studyExtService,
        Map.of(
            "create",
            AuthAnnotationSpec.withPortalPerm(
                AuthUtilService.BASE_PERMISSION, List.of(SuperuserOnly.class)),
            "delete",
            AuthAnnotationSpec.withPortalStudyPerm(
                AuthUtilService.BASE_PERMISSION, List.of(SuperuserOnly.class)),
            "getStudiesWithEnvs",
            AuthAnnotationSpec.withPortalPerm(AuthUtilService.BASE_PERMISSION),
            "updateStudy",
            AuthAnnotationSpec.withPortalStudyPerm("study_settings_edit")));
  }

  @Test
  @Transactional
  public void testStudyCreation(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
    Portal portal = portalFactory.buildPersisted(getTestName(testInfo));
    String newStudyShortcode = "newStudy" + RandomStringUtils.randomAlphabetic(5);
    StudyCreationDto studyDto = new StudyCreationDto(newStudyShortcode, "the new study");
    studyExtService.create(PortalAuthContext.of(operator, portal.getShortcode()), studyDto);

    // confirm study and environments were created
    Study study = studyService.findByShortcode(newStudyShortcode).get();
    assertThat(study.getName(), equalTo(studyDto.getName()));
    List<StudyEnvironment> newEnvs = studyEnvironmentService.findByStudy(study.getId());
    assertThat(newEnvs.size(), equalTo(3));
  }

  @Test
  @Transactional
  public void testStudyDeletion(TestInfo testInfo) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(testInfo), true);
    Portal portal = portalFactory.buildPersisted(getTestName(testInfo));
    String newStudyShortcode = "newStudy" + RandomStringUtils.randomAlphabetic(5);
    StudyCreationDto studyDto = new StudyCreationDto(newStudyShortcode, "the new study");
    studyExtService.create(PortalAuthContext.of(operator, portal.getShortcode()), studyDto);

    // confirm study was deleted
    studyExtService.delete(
        PortalStudyAuthContext.of(operator, portal.getShortcode(), newStudyShortcode));
    assertThat(studyService.findByShortcode(newStudyShortcode).isEmpty(), equalTo(true));
    // confirm that the corresponding portalService was also deleted
    assertThat(portalStudyService.findByPortalId(portal.getId()), empty());
  }

  @Test
  @Transactional
  void testCreateWithTemplate(TestInfo info) {
    Portal portal = portalFactory.buildPersistedWithEnvironments(getTestName(info));

    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), true);
    Study study =
        studyExtService.create(
            PortalAuthContext.of(operator, portal.getShortcode()),
            StudyCreationDto.builder()
                .shortcode("testshortcode")
                .name("Test Study")
                .template(StudyCreationDto.StudyTemplate.BASIC)
                .build());

    Assertions.assertEquals("Test Study", study.getName());
    Assertions.assertEquals("testshortcode", study.getShortcode());

    StudyEnvironment sandboxEnv =
        study.getStudyEnvironments().stream()
            .filter(env -> env.getEnvironmentName().equals(EnvironmentName.sandbox))
            .findFirst()
            .orElseThrow();

    Assertions.assertEquals(6, triggerService.findByStudyEnvironmentId(sandboxEnv.getId()).size());
  }

  @Test
  public void testOnlySuperuserCanUpdateShortcode(TestInfo info) {
    baseSeedPopulator.populateRolesAndPermissions();
    Portal portal = portalFactory.buildPersistedWithEnvironments(getTestName(info));
    AdminUserBundle operatorBundle =
        portalAdminUserFactory.buildPersistedWithRoles(
            getTestName(info), portal, List.of("study_admin"));
    AdminUser operator = operatorBundle.user();
    Study study = studyFactory.buildPersisted(portal.getId(), getTestName(info));

    Assertions.assertThrows(
        PermissionDeniedException.class,
        () ->
            studyExtService.updateStudy(
                PortalStudyAuthContext.of(operator, portal.getShortcode(), study.getShortcode()),
                Study.builder().shortcode("newShortcode").name("newName").build()));

    // Confirm that the study was not updated
    Study updatedStudy = studyService.findByShortcode(study.getShortcode()).get();
    assertThat(updatedStudy.getShortcode(), equalTo(study.getShortcode()));

    studyExtService.updateStudy(
        PortalStudyAuthContext.of(operator, portal.getShortcode(), study.getShortcode()),
        Study.builder().shortcode(study.getShortcode()).name("newName").build());

    // Confirm that the study was updated
    updatedStudy = studyService.findByShortcode(study.getShortcode()).get();

    assertThat(updatedStudy.getName(), equalTo("newName"));

    AdminUser superuser = adminUserFactory.buildPersisted(getTestName(info), true);

    studyExtService.updateStudy(
        PortalStudyAuthContext.of(superuser, portal.getShortcode(), study.getShortcode()),
        Study.builder().shortcode("newShortcode").name("newName").build());

    // Confirm that the study was updated
    updatedStudy = studyService.findByShortcode("newShortcode").get();
    assertThat(updatedStudy.getId(), equalTo(study.getId()));
  }
}
