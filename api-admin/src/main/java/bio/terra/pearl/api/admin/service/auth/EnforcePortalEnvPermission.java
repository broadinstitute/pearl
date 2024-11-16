package bio.terra.pearl.api.admin.service.auth;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EnforcePortalEnvPermission {
  String permission();
}
