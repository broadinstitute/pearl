package bio.terra.pearl.api.admin.service;

import bio.terra.common.exception.UnauthorizedException;
import bio.terra.common.iam.BearerTokenFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.PortalStudyService;
import com.auth0.jwt.JWT;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/** Utility service for common auth-related methods */
@Service
public class AuthUtilService {
  private CurrentUnauthedUserService currentUnauthedUserService;
  private BearerTokenFactory bearerTokenFactory;
  private PortalService portalService;
  private PortalStudyService portalStudyService;

  public AuthUtilService(
      CurrentUnauthedUserService currentUnauthedUserService,
      BearerTokenFactory bearerTokenFactory,
      PortalService portalService,
      PortalStudyService portalStudyService) {
    this.currentUnauthedUserService = currentUnauthedUserService;
    this.bearerTokenFactory = bearerTokenFactory;
    this.portalService = portalService;
    this.portalStudyService = portalStudyService;
  }

  /** gets the user from the request, throwing an exception if not present */
  public AdminUser requireAdminUser(HttpServletRequest request) {
    String token = bearerTokenFactory.from(request).getToken();
    var decodedJWT = JWT.decode(token);
    var email = decodedJWT.getClaim("email").asString();
    Optional<AdminUser> userOpt = currentUnauthedUserService.findByUsername(email);
    if (userOpt.isEmpty()) {
      throw new UnauthorizedException("User not found: " + email);
    }
    return userOpt.get();
  }

  public Portal authUserToPortal(AdminUser user, String portalShortcode) {
    return portalService.authAdminToPortal(user, portalShortcode);
  }

  public PortalStudy authUserToStudy(
      AdminUser user, String portalShortcode, String studyShortcode) {
    Portal portal = authUserToPortal(user, portalShortcode);
    Optional<PortalStudy> portalStudy =
        portalStudyService.findStudyInPortal(studyShortcode, portal.getId());
    if (portalStudy.isEmpty()) {
      throw new PermissionDeniedException(
          "User %s does not have permissions on study %s"
              .formatted(user.getUsername(), studyShortcode));
    }
    return portalStudy.get();
  }
}
