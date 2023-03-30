package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.NotificationsApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.notification.Notification;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.service.notification.NotificationConfigService;
import bio.terra.pearl.core.service.notification.NotificationDispatcher;
import bio.terra.pearl.core.service.notification.NotificationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationsController implements NotificationsApi {
  private AuthUtilService authUtilService;
  private NotificationService notificationService;
  private NotificationConfigService notificationConfigService;
  private NotificationDispatcher notificationDispatcher;
  private EnrolleeService enrolleeService;
  private ObjectMapper objectMapper;
  private HttpServletRequest request;

  public NotificationsController(
      AuthUtilService authUtilService,
      NotificationService notificationService,
      NotificationConfigService notificationConfigService,
      NotificationDispatcher notificationDispatcher,
      EnrolleeService enrolleeService,
      ObjectMapper objectMapper,
      HttpServletRequest request) {
    this.authUtilService = authUtilService;
    this.notificationService = notificationService;
    this.notificationConfigService = notificationConfigService;
    this.notificationDispatcher = notificationDispatcher;
    this.enrolleeService = enrolleeService;
    this.objectMapper = objectMapper;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> find(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    authUtilService.authUserToStudy(adminUser, portalShortcode, studyShortcode);
    Enrollee enrollee = enrolleeService.findOneByShortcode(enrolleeShortcode).get();
    List<Notification> notifications = notificationService.findByEnrolleeId(enrollee.getId());
    return ResponseEntity.ok(notifications);
  }

  @Override
  public ResponseEntity<Object> test(
      String portalShortcode, String envName, UUID configId, Object body) {
    AdminUser adminUser = authUtilService.requireAdminUser(request);
    authUtilService.authUserToPortal(adminUser, portalShortcode);
    EnrolleeRuleData enrolleeRuleData = objectMapper.convertValue(body, EnrolleeRuleData.class);
    NotificationConfig config = notificationConfigService.find(configId).get();
    try {
      notificationDispatcher.dispatchTestNotification(config, enrolleeRuleData);
      return ResponseEntity.ok(config);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body(e);
    }
  }
}
