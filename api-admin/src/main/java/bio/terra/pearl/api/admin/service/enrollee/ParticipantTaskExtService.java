package bio.terra.pearl.api.admin.service.enrollee;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.exception.StudyEnvironmentMissing;
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

  public List<ParticipantTask> findAll(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      String stableId,
      AdminUser operator) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);
    return participantTaskService.findTasksByStudyAndTarget(studyEnv.getId(), List.of(stableId));
  }

  public List<ParticipantTask> assignToEnrollees(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      ParticipantTaskAssignDto assignDto,
      AdminUser operator) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);

    if (assignDto.taskType().equals(TaskType.SURVEY)) {
      return surveyTaskDispatcher.assign(
          assignDto, studyEnv.getId(), new ResponsibleEntity(operator));
    }
    throw new UnsupportedOperationException(
        "task type %s not supported".formatted(assignDto.taskType()));
  }

  /**
   * applies the task updates to the given environment. Returns a list of the updated tasks This is
   * assumed to be a relatively rare operation, so this is not particularly optimized for
   * performance.
   */
  public List<ParticipantTask> updateTasks(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      ParticipantTaskUpdateDto updateDto,
      AdminUser operator) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnv =
        studyEnvironmentService
            .findByStudy(studyShortcode, environmentName)
            .orElseThrow(StudyEnvironmentMissing::new);
    List<ParticipantTask> updatedTasks =
        participantTaskService.updateTasks(
            studyEnv.getId(), updateDto, new ResponsibleEntity(operator));
    return updatedTasks;
  }

  public ParticipantTaskService.ParticipantTaskTaskListDto getByStudyEnvironment(
      String portalShortcode,
      String studyShortcode,
      EnvironmentName environmentName,
      List<String> includedRelations,
      AdminUser operator) {
    authUtilService.authUserToStudy(operator, portalShortcode, studyShortcode);
    StudyEnvironment studyEnvironment =
        studyEnvironmentService.findByStudy(studyShortcode, environmentName).get();
    return participantTaskService.findAdminTasksByStudyEnvironmentId(
        studyEnvironment.getId(), includedRelations);
  }

  public List<ParticipantTask> getByEnrollee(String enrolleeShortcode, AdminUser operator) {
    Enrollee enrollee = authUtilService.authAdminUserToEnrollee(operator, enrolleeShortcode);
    return participantTaskService.findByEnrolleeId(enrollee.getId()).stream()
        .filter(task -> task.getTaskType().equals(TaskType.ADMIN_NOTE))
        .toList();
  }

  /** current we only allow editing admin assignment and task status. */
  @EnforcePortalEnrolleePermission(permission = "participant_data_edit")
  public ParticipantTask update(
      PortalEnrolleeAuthContext authContext, ParticipantTask updatedTask) {
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
            .build();
    return participantTaskService.update(taskToUpdate, auditInfo);
  }
}
