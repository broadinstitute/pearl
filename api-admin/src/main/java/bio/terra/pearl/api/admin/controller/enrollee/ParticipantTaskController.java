package bio.terra.pearl.api.admin.controller.enrollee;

import bio.terra.pearl.api.admin.api.ParticipantTaskApi;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.api.admin.service.enrollee.ParticipantTaskExtService;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.service.workflow.ParticipantTaskAssignDto;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ParticipantTaskController implements ParticipantTaskApi {
  private AuthUtilService authUtilService;
  private ParticipantTaskExtService participantTaskExtService;
  private HttpServletRequest request;
  private ObjectMapper objectMapper;

  public ParticipantTaskController(
      AuthUtilService authUtilService,
      ParticipantTaskExtService participantTaskExtService,
      HttpServletRequest request,
      ObjectMapper objectMapper) {
    this.authUtilService = authUtilService;
    this.participantTaskExtService = participantTaskExtService;
    this.request = request;
    this.objectMapper = objectMapper;
  }

  @Override
  public ResponseEntity<Object> updateAll(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ParticipantTaskUpdateDto updateDto =
        objectMapper.convertValue(body, ParticipantTaskUpdateDto.class);
    List<ParticipantTask> participantTasks =
        participantTaskExtService.updateTasks(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            updateDto);
    return ResponseEntity.ok(participantTasks);
  }

  @Override
  public ResponseEntity<Object> assignToEnrollees(
      String portalShortcode, String studyShortcode, String envName, Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ParticipantTaskAssignDto assignDto =
        objectMapper.convertValue(body, ParticipantTaskAssignDto.class);
    List<ParticipantTask> participantTasks =
        participantTaskExtService.assignToEnrollees(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            assignDto);
    return ResponseEntity.ok(participantTasks);
  }

  @Override
  public ResponseEntity<Object> findAll(
      String portalShortcode, String studyShortcode, String envName, String targetStableId) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<ParticipantTask> participantTasks =
        participantTaskExtService.findAll(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            targetStableId);
    return ResponseEntity.ok(participantTasks);
  }

  @Override
  public ResponseEntity<Object> getByStudyEnvironment(
      String portalShortcode, String studyShortcode, String envName, String include) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<String> includedRelations = List.of();
    if (!StringUtils.isBlank(include)) {
      includedRelations = List.of(include.split(","));
    }
    ParticipantTaskService.ParticipantTaskTaskListDto tasks =
        participantTaskExtService.getByStudyEnvironment(
            PortalStudyEnvAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName),
            includedRelations);
    return ResponseEntity.ok(tasks);
  }

  @Override
  public ResponseEntity<Object> getByEnrollee(
      String portalShortcode, String studyShortcode, String envName, String enrolleeShortcode) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    List<ParticipantTask> tasks =
        participantTaskExtService.getByEnrollee(
            PortalEnrolleeAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName, enrolleeShortcode));
    return ResponseEntity.ok(tasks);
  }

  @Override
  public ResponseEntity<Object> update(
      String portalShortcode,
      String studyShortcode,
      String envName,
      String enrolleeShortcodeOrId,
      UUID taskId,
      Object body) {
    AdminUser operator = authUtilService.requireAdminUser(request);
    EnvironmentName environmentName = EnvironmentName.valueOfCaseInsensitive(envName);
    ParticipantTaskUpdate taskUpdate = objectMapper.convertValue(body, ParticipantTaskUpdate.class);
    taskUpdate.task.setId(taskId);
    ParticipantTask updatedTask =
        participantTaskExtService.update(
            PortalEnrolleeAuthContext.of(
                operator, portalShortcode, studyShortcode, environmentName, enrolleeShortcodeOrId),
            taskUpdate.task,
            taskUpdate.justification);
    return ResponseEntity.ok(updatedTask);
  }

  public record ParticipantTaskUpdate(ParticipantTask task, String justification) {}
}
