package bio.terra.pearl.api.admin.service;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.portal.MailingListContact;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.MailingListContactService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailingListExtService {
  private MailingListContactService mailingListContactService;

  public MailingListExtService(MailingListContactService mailingListContactService) {
    this.mailingListContactService = mailingListContactService;
  }

  @EnforcePortalEnvPermission(permission = AuthUtilService.BASE_PERMISSION)
  public List<MailingListContact> getAll(PortalEnvAuthContext authContext) {
    return mailingListContactService.findByPortalEnv(authContext.getPortalEnvironment().getId());
  }

  @Transactional
  @EnforcePortalEnvPermission(permission = AuthUtilService.BASE_PERMISSION)
  public List<MailingListContact> create(
      PortalEnvAuthContext authContext, List<MailingListContact> contacts) {
    DataAuditInfo auditInfo =
        DataAuditInfo.builder().responsibleAdminUserId(authContext.getOperator().getId()).build();
    List<MailingListContact> newContacts =
        mailingListContactService.bulkCreate(
            authContext.getPortalEnvironment().getId(), contacts, auditInfo);

    return newContacts;
  }

  @Transactional
  @EnforcePortalEnvPermission(permission = AuthUtilService.BASE_PERMISSION)
  public void delete(PortalEnvAuthContext authContext, UUID contactId) {
    MailingListContact contact = mailingListContactService.find(contactId).get();
    if (!contact.getPortalEnvironmentId().equals(authContext.getPortalEnvironment().getId())) {
      throw new PermissionDeniedException("Contact does not belong to the given portal");
    }
    DataAuditInfo auditInfo =
        DataAuditInfo.builder().responsibleAdminUserId(authContext.getOperator().getId()).build();
    mailingListContactService.delete(contactId, auditInfo, CascadeProperty.EMPTY_SET);
  }
}
