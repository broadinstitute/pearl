package bio.terra.pearl.api.admin;

import bio.terra.pearl.api.admin.service.auth.*;
import java.lang.annotation.Annotation;
import java.util.List;

public record AuthAnnotationSpec(
    Class<? extends Annotation> permissionAnnotationClass,
    String permissionName,
    List<Class<? extends Annotation>> otherAnnotations) {

  public static AuthAnnotationSpec withPortalPerm(String permissionName) {
    return new AuthAnnotationSpec(EnforcePortalPermission.class, permissionName, List.of());
  }

  public static AuthAnnotationSpec withPortalPerm(
      String permissionName, List<Class<? extends Annotation>> otherAnnotations) {
    return new AuthAnnotationSpec(EnforcePortalPermission.class, permissionName, otherAnnotations);
  }

  public static AuthAnnotationSpec withPortalStudyPerm(String permissionName) {
    return new AuthAnnotationSpec(EnforcePortalStudyPermission.class, permissionName, List.of());
  }

  public static AuthAnnotationSpec withPortalStudyPerm(
      String permissionName, List<Class<? extends Annotation>> otherAnnotations) {
    return new AuthAnnotationSpec(
        EnforcePortalStudyPermission.class, permissionName, otherAnnotations);
  }

  public static AuthAnnotationSpec withPortalEnvPerm(String permissionName) {
    return withPortalEnvPerm(permissionName, List.of());
  }

  public static AuthAnnotationSpec withPortalEnvPerm(
      String permissionName, List<Class<? extends Annotation>> otherAnnotations) {
    return new AuthAnnotationSpec(
        EnforcePortalEnvPermission.class, permissionName, otherAnnotations);
  }

  public static AuthAnnotationSpec withPortalEnrolleePerm(String permissionName) {
    return new AuthAnnotationSpec(EnforcePortalEnrolleePermission.class, permissionName, List.of());
  }

  public static AuthAnnotationSpec withPortalStudyEnvPerm(String permissionName) {
    return withPortalStudyEnvPerm(permissionName, List.of());
  }

  public static AuthAnnotationSpec withPortalStudyEnvPerm(
      String permissionName, List<Class<? extends Annotation>> otherAnnotations) {
    return new AuthAnnotationSpec(
        EnforcePortalStudyEnvPermission.class, permissionName, otherAnnotations);
  }

  public static AuthAnnotationSpec withOtherAnnotations(
      List<Class<? extends Annotation>> otherAnnotations) {
    return new AuthAnnotationSpec(null, null, otherAnnotations);
  }

  public static AuthAnnotationSpec withPublicAnnotation() {
    return new AuthAnnotationSpec(null, null, List.of(Public.class));
  }
}
