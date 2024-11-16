package bio.terra.pearl.api.admin.service.auth;

import bio.terra.pearl.api.admin.service.auth.context.PortalEnvAuthContext;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.portal.exception.PortalEnvironmentMissing;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;

public class EnforcePortalEnvPermissionAspect
    extends BaseEnforcePermissionAspect<PortalEnvAuthContext, EnforcePortalEnvPermission> {
  private final AuthUtilService authUtilService;
  private final PortalEnvironmentService portalEnvironmentService;

  public EnforcePortalEnvPermissionAspect(
      AuthUtilService authUtilService, PortalEnvironmentService portalEnvironmentService) {
    this.authUtilService = authUtilService;
    this.portalEnvironmentService = portalEnvironmentService;
  }

  @Around(value = "@annotation(EnforcePortalEnvPermission)")
  public Object enforcePermission(ProceedingJoinPoint joinPoint) throws Throwable {
    PortalEnvAuthContext authContext = extractAuthContext(joinPoint);
    String permission = getPermissionName(joinPoint);
    Portal portal =
        authUtilService.authUserToPortalWithPermission(
            authContext.getOperator(), authContext.getPortalShortcode(), permission);
    authContext.setPortal(portal);

    PortalEnvironment portalEnv =
        portalEnvironmentService
            .findOne(authContext.getPortalShortcode(), authContext.getEnvironmentName())
            .orElseThrow(PortalEnvironmentMissing::new);
    authContext.setPortalEnvironment(portalEnv);
    return joinPoint.proceed();
  }

  @Override
  protected String getPermissionName(ProceedingJoinPoint joinPoint) {
    return ((MethodSignature) joinPoint.getSignature())
        .getMethod()
        .getAnnotation(getAnnotationClass())
        .permission();
  }

  @Override
  protected Class<PortalEnvAuthContext> getAuthContextClass() {
    return PortalEnvAuthContext.class;
  }

  @Override
  protected Class<EnforcePortalEnvPermission> getAnnotationClass() {
    return EnforcePortalEnvPermission.class;
  }
}
