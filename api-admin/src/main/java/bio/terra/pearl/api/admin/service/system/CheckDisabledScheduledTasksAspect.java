package bio.terra.pearl.api.admin.service.system;

import bio.terra.pearl.core.service.system.SystemSettingsService;
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
  private final SystemSettingsService maintenanceModeService;

  public CheckDisabledScheduledTasksAspect(SystemSettingsService maintenanceModeService) {
    this.maintenanceModeService = maintenanceModeService;
  }

  // Since this annotation is already aware of the method name, we could eventually
  // allow for a more fine-grained control of which tasks are disabled. But right now,
  // we're just doing a global disable of all scheduled tasks.
  @Around("@annotation(bio.terra.pearl.api.admin.service.system.CheckDisableScheduledTasks)")
  public Object checkMaintenanceMode(ProceedingJoinPoint joinPoint) throws Throwable {
    boolean disableScheduledTask =
        maintenanceModeService.getSystemSettings().isDisableScheduledJobs();
    if (disableScheduledTask) {
      String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
      log.info("Scheduled tasks have been disabled. Skipping task: {}", methodName);
      return null;
    }
    return joinPoint.proceed();
  }
}
