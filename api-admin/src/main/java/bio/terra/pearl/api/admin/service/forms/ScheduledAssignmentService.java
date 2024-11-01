package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.core.service.workflow.TaskDispatcher;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Slf4j
public class ScheduledAssignmentService {
  private final List<TaskDispatcher> taskDispatchers;

  public ScheduledAssignmentService(List<TaskDispatcher> taskDispatchers) {
    this.taskDispatchers = taskDispatchers;
  }

  @Scheduled(
      fixedDelay = 60 * 60 * 1000,
      initialDelay = 5 * 1000) // wait an hour between executions, start after 5 seconds
  @SchedulerLock(
      name = "ScheduledSurveyAssignmentService.assignScheduledSurveys",
      lockAtMostFor = "500s",
      lockAtLeastFor = "10s")
  public void assignScheduledSurveys() {
    log.info("Scheduled task processing beginning");
    taskDispatchers.forEach(TaskDispatcher::assignScheduledTasks);
    log.info("Scheduled task processing complete");
  }
}
