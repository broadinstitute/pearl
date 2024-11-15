package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalStudyEnvPermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalStudyEnvAuthContext;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.survey.SurveyTaskDispatcher;
import bio.terra.pearl.core.service.workflow.ParticipantTaskAssignDto;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskUpdateDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ParticipantTaskExtService {
  private final ParticipantTaskService participantTaskService;
  private final StudyEnvironmentService studyEnvironmentService;
  private final AuthUtilService authUtilService;
  private final SurveyTaskDispatcher surveyTaskDispatcher;

  public ParticipantTaskExtService(
      ParticipantTaskService participantTaskService,
      StudyEnvironmentService studyEnvironmentService,
      AuthUtilService authUtilService,
      SurveyTaskDispatcher surveyTaskDispatcher1) {
    this.participantTaskService = participantTaskService;
    this.studyEnvironmentService = studyEnvironmentService;
    this.authUtilService = authUtilService;
    this.surveyTaskDispatcher = surveyTaskDispatcher1;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public List<ParticipantTask> findAll(PortalStudyEnvAuthContext authContext, String stableId) {
    return participantTaskService.findTasksByStudyAndTarget(
        authContext.getStudyEnvironment().getId(), List.of(stableId));
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  public List<ParticipantTask> assignToEnrollees(
      PortalStudyEnvAuthContext authContext, ParticipantTaskAssignDto assignDto) {
    if (assignDto.taskType().equals(TaskType.SURVEY)) {
      return surveyTaskDispatcher.assign(
          assignDto,
          authContext.getStudyEnvironment().getId(),
          new ResponsibleEntity(authContext.getOperator()));
    }
    throw new UnsupportedOperationException(
        "task type %s not supported".formatted(assignDto.taskType()));
  }

  /**
   * applies the task updates to the given environment. Returns a list of the updated tasks. This is
   * assumed to be a relatively rare operation, so this is not particularly optimized for
   * performance.
   */
  @EnforcePortalStudyEnvPermission(permission = "participant_data_edit")
  public List<ParticipantTask> updateTasks(
      PortalStudyEnvAuthContext authContext, ParticipantTaskUpdateDto updateDto) {
    List<ParticipantTask> updatedTasks =
        participantTaskService.updateTasks(
            authContext.getStudyEnvironment().getId(),
            updateDto,
            new ResponsibleEntity(authContext.getOperator()));
    return updatedTasks;
  }

  @EnforcePortalStudyEnvPermission(permission = "participant_data_view")
  public ParticipantTaskService.ParticipantTaskTaskListDto getByStudyEnvironment(
      PortalStudyEnvAuthContext authContext, List<String> includedRelations) {
    return participantTaskService.findAdminTasksByStudyEnvironmentId(
        authContext.getStudyEnvironment().getId(), includedRelations);
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_view")
  public List<ParticipantTask> getByEnrollee(PortalEnrolleeAuthContext authContext) {
    return participantTaskService.findByEnrolleeId(authContext.getEnrollee().getId()).stream()
        .filter(task -> task.getTaskType().equals(TaskType.ADMIN_NOTE))
        .toList();
  }

  /** current we only allow editing admin assignment and task status. */
  @EnforcePortalEnrolleePermission(permission = "participant_data_edit")
  public ParticipantTask update(
      PortalEnrolleeAuthContext authContext, ParticipantTask updatedTask, String justification) {
    ParticipantTask taskToUpdate =
        participantTaskService
            .find(updatedTask.getId())
            .orElseThrow(
                () -> new NotFoundException("task %s not found".formatted(updatedTask.getId())));
    if (!taskToUpdate.getEnrolleeId().equals(authContext.getEnrollee().getId())) {
      throw new IllegalArgumentException(
          "task does not belong to enrollee %s"
              .formatted(authContext.getEnrollee().getShortcode()));
    }
    taskToUpdate.setAssignedAdminUserId(updatedTask.getAssignedAdminUserId());
    taskToUpdate.setStatus(updatedTask.getStatus());
    DataAuditInfo auditInfo =
        DataAuditInfo.builder()
            .responsibleAdminUserId(authContext.getOperator().getId())
            .enrolleeId(authContext.getEnrollee().getId())
            .portalParticipantUserId(taskToUpdate.getPortalParticipantUserId())
            .justification(justification)
            .build();
    return participantTaskService.update(taskToUpdate, auditInfo);
  }
}
