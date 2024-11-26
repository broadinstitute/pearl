package bio.terra.pearl.api.admin.service.system;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import bio.terra.juniper.core.model.maintenance.SystemSettings;
import bio.terra.pearl.core.service.system.SystemSettingsService;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CheckDisableScheduledTaskAspectTests {

  @Mock private SystemSettingsService systemSettingsService;
  @Mock private ProceedingJoinPoint joinPoint;
  @Mock private MethodSignature methodSignature;
  @InjectMocks private CheckDisableScheduledTaskAspect aspect;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testCheckScheduledTasksDisabled() throws Throwable {
    SystemSettings systemSettings = mock(SystemSettings.class);
    when(systemSettingsService.getSystemSettings()).thenReturn(systemSettings);
    when(systemSettings.isDisableScheduledJobs()).thenReturn(true);

    Method method =
        CheckDisableScheduledTaskAspectTests.class.getMethod("testCheckScheduledTasksDisabled");
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(method);

    Object result = aspect.checkTasksDisabled(joinPoint);

    assertNull(result);
    verify(systemSettingsService).getSystemSettings();
    verify(joinPoint, never()).proceed();
  }

  @Test
  public void testCheckScheduledTasksEnabled() throws Throwable {
    SystemSettings systemSettings = mock(SystemSettings.class);
    when(systemSettingsService.getSystemSettings()).thenReturn(systemSettings);
    when(systemSettings.isDisableScheduledJobs()).thenReturn(false);

    Method method =
        CheckDisableScheduledTaskAspectTests.class.getMethod("testCheckScheduledTasksEnabled");
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(method);

    aspect.checkTasksDisabled(joinPoint);

    verify(systemSettingsService).getSystemSettings();
    verify(joinPoint).proceed();
  }
}
