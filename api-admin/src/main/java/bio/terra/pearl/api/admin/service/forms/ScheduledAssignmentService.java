package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.core.service.workflow.TaskConfigDispatcher;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduledAssignmentService {
  private final List<TaskConfigDispatcher> taskDispatchers;

  public ScheduledAssignmentService(List<TaskConfigDispatcher> taskDispatchers) {
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
    taskDispatchers.forEach(TaskConfigDispatcher::assignScheduledTasks);
    log.info("Scheduled task processing complete");
  }
}