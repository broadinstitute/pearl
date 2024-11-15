package bio.terra.pearl.api.admin.service.maintenance;

import bio.terra.pearl.core.service.maintenance.MaintenanceModeService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CheckDisabledScheduledTasksAspect {
  private final MaintenanceModeService maintenanceModeService;

  public CheckDisabledScheduledTasksAspect(MaintenanceModeService maintenanceModeService) {
    this.maintenanceModeService = maintenanceModeService;
  }

  // Since this annotation is already aware of the method name, we could eventually
  // allow for a more fine-grained control of which tasks are disabled. But right now,
  // we're just doing a global disable of all scheduled tasks.
  @Around("@annotation(bio.terra.pearl.api.admin.service.maintenance.CheckDisableScheduledTasks)")
  public Object checkMaintenanceMode(ProceedingJoinPoint joinPoint) throws Throwable {
    Boolean disableScheduledTask =
        (Boolean)
            maintenanceModeService
                .getMaintenanceModeSettings()
                .getOrDefault("disableScheduledJobs", false);
    if (disableScheduledTask) {
      String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
      log.info("Scheduled tasks have been disabled. Skipping task: {}", methodName);
      return null;
    }
    return joinPoint.proceed();
  }
}
