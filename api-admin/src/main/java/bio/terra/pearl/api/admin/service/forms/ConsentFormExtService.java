package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class ConsentFormExtService {
  private AuthUtilService authUtilService;
  private ConsentFormService consentFormService;
  private StudyEnvironmentConsentService studyEnvironmentConsentService;

  public ConsentFormExtService(
      AuthUtilService authUtilService,
      ConsentFormService consentFormService,
      StudyEnvironmentConsentService studyEnvironmentConsentService) {
    this.authUtilService = authUtilService;
    this.consentFormService = consentFormService;
    this.studyEnvironmentConsentService = studyEnvironmentConsentService;
  }

  public ConsentForm createNewVersion(
      String portalShortcode, ConsentForm consentForm, AdminUser user) {
    if (!user.isSuperuser()) {
      throw new PermissionDeniedException("You do not have permissions to perform this operation");
    }
    Portal portal = authUtilService.authUserToPortal(user, portalShortcode);
    return consentFormService.createNewVersion(portal.getId(), consentForm);
  }

  public StudyEnvironmentConsent updateConfiguredConsent(
      String portalShortcode,
      EnvironmentName envName,
      StudyEnvironmentConsent updatedObj,
      AdminUser user) {
    authUtilService.authUserToPortal(user, portalShortcode);
    if (!EnvironmentName.sandbox.equals(envName)) {
      throw new IllegalArgumentException(
          "Updates can only be made directly to the sandbox environment".formatted(envName));
    }
    StudyEnvironmentConsent existing =
        studyEnvironmentConsentService.find(updatedObj.getId()).get();
    BeanUtils.copyProperties(updatedObj, existing);
    return studyEnvironmentConsentService.update(existing);
  }
}
