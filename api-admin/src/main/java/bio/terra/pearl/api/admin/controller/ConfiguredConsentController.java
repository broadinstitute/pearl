package bio.terra.pearl.api.admin.controller;

import bio.terra.pearl.api.admin.api.ConfiguredConsentApi;
import bio.terra.pearl.api.admin.model.ConfiguredConsentDto;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ConfiguredConsentController implements ConfiguredConsentApi {
  private AuthUtilService requestService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;
  private StudyEnvironmentConsentService studyEnvConsentService;

  public ConfiguredConsentController(
      AuthUtilService requestService,
      HttpServletRequest request,
      ObjectMapper objectMapper,
      StudyEnvironmentConsentService studyEnvConsentService) {
    this.requestService = requestService;
    this.request = request;
    this.objectMapper = objectMapper;
    this.studyEnvConsentService = studyEnvConsentService;
  }

  @Override
  public ResponseEntity<ConfiguredConsentDto> patch(
      String portalShortcode,
      String studyShortcode,
      String envName,
      UUID configuredConsentId,
      ConfiguredConsentDto body) {
    AdminUser adminUser = requestService.requireAdminUser(request);
    requestService.authUserToPortal(adminUser, portalShortcode);

    StudyEnvironmentConsent configuredSurvey =
        objectMapper.convertValue(body, StudyEnvironmentConsent.class);
    StudyEnvironmentConsent existing = studyEnvConsentService.find(configuredSurvey.getId()).get();
    BeanUtils.copyProperties(body, existing);
    StudyEnvironmentConsent savedConsent = studyEnvConsentService.update(adminUser, existing);
    return ResponseEntity.ok(objectMapper.convertValue(savedConsent, ConfiguredConsentDto.class));
  }
}
