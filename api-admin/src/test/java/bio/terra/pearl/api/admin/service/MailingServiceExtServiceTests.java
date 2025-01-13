package bio.terra.pearl.api.admin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import bio.terra.pearl.api.admin.AuthAnnotationSpec;
import bio.terra.pearl.api.admin.AuthTestUtils;
import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.MailingListContactFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class MailingServiceExtServiceTests extends BaseSpringBootTest {
  @Autowired MailingListExtService mailingListExtService;
  @Autowired MailingListContactService mailingListService;
  @Autowired PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired MailingListContactFactory mailingListContactFactory;
  @Autowired AdminUserFactory adminUserFactory;
  @Autowired PortalService portalService;
  @Autowired ParticipantDataChangeService participantDataChangeService;
  @Autowired ObjectMapper objectMapper;

  @Test
  public void testAllMethodsAnnotated() {
    AuthTestUtils.assertAllMethodsAnnotated(
        mailingListExtService,
        Map.of(
            "getAll",
            AuthAnnotationSpec.withPortalEnvPerm(AuthUtilService.BASE_PERMISSION),
            "create",
            AuthAnnotationSpec.withPortalEnvPerm(AuthUtilService.BASE_PERMISSION),
            "delete",
            AuthAnnotationSpec.withPortalEnvPerm(AuthUtilService.BASE_PERMISSION)));
  }

  @Test
  @Transactional
  public void deleteMailingListContact(TestInfo info) throws Exception {
    PortalEnvironment portalEnvironment =
        portalEnvironmentFactory.buildPersisted(getTestName(info));
    AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info), true);
    Portal portal = portalService.find(portalEnvironment.getPortalId()).get();
    DataAuditInfo auditInfo =
        DataAuditInfo.builder().responsibleAdminUserId(adminUser.getId()).build();
    MailingListContact contact =
        mailingListService.create(
            MailingListContact.builder()
                .name("test1")
                .email("test1@test.com")
                .portalEnvironmentId(portalEnvironment.getId())
                .build(),
            auditInfo);

    mailingListExtService.delete(
        PortalEnvAuthContext.of(
            adminUser, portal.getShortcode(), portalEnvironment.getEnvironmentName()),
        contact.getId());
    assertThat(mailingListService.find(contact.getId()).isPresent(), equalTo(false));
  }

  @Test
  @Transactional
  public void bulkCreateMailingListContacts(TestInfo info) {
    PortalEnvironment portalEnvironment =
        portalEnvironmentFactory.buildPersisted(getTestName(info));
    AdminUser adminUser = adminUserFactory.buildPersisted(getTestName(info), true);
    Portal portal = portalService.find(portalEnvironment.getPortalId()).get();
    List<MailingListContact> contacts =
        List.of(
            MailingListContact.builder().name("Jonas Salk").email("jsalk@test.com").build(),
            MailingListContact.builder().name("Basic Done").email("basic@test.com").build());

    List<MailingListContact> createdContacts =
        mailingListExtService.create(
            PortalEnvAuthContext.of(
                adminUser, portal.getShortcode(), portalEnvironment.getEnvironmentName()),
            contacts);

    assertThat(createdContacts, hasSize(2));
  }
}
