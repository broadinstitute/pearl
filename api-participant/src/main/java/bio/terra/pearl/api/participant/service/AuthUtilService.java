package bio.terra.pearl.api.participant.service;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.PermissionDeniedException;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.portal.PortalWithPortalUser;
import jakarta.ws.rs.NotAuthorizedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthUtilService {
  private EnrolleeService enrolleeService;
  private EnrolleeRelationService enrolleeRelationService;
  private PortalService portalService;
  private PortalParticipantUserService portalParticipantUserService;

  public AuthUtilService(
      EnrolleeService enrolleeService,
      PortalService portalService,
      PortalParticipantUserService portalParticipantUserService,
      EnrolleeRelationService enrolleeRelationService) {
    this.enrolleeService = enrolleeService;
    this.portalService = portalService;
    this.portalParticipantUserService = portalParticipantUserService;
    this.enrolleeRelationService = enrolleeRelationService;
  }

  /**
   * returns the enrollee if the user is authorized to access/modify it, throws an error otherwise
   */
  public Enrollee authParticipantUserToEnrollee(UUID participantUserId, String enrolleeShortcode) {
    // for now, a user is only allowed to access an enrollee if it's themself.  Later, we'll add
    // proxies
    Optional<Enrollee> enrolleeOpt =
        enrolleeService.findByEnrolleeId(participantUserId, enrolleeShortcode);
    if (enrolleeOpt.isEmpty()) {
      throw new PermissionDeniedException("Access denied for %s".formatted(enrolleeShortcode));
    }
    return enrolleeOpt.get();
  }

  public PortalParticipantUser authParticipantUserToPortalParticipantUser(
      UUID participantUserId, UUID portalParticipantUserId) {
    PortalParticipantUser ppUser =
        portalParticipantUserService
                .find(portalParticipantUserId)
                .orElseThrow(() -> new NotFoundException("Portal participant user not found."));

    if (ppUser.getParticipantUserId().equals(participantUserId)) {
      return ppUser;
    }

    // participant user was not the ppUser, but could be a proxy.
    // check if proxy exists on any of the enrollees; if so, then
    // they have access to this ppUser.
    List<Enrollee> enrolleeList = enrolleeService.findByPortalParticipantUser(ppUser);

    if (enrolleeRelationService.isUserProxyForAnyOf(participantUserId, enrolleeList)) {
      return ppUser;
    }

    throw new NotAuthorizedException("Cannot act on behalf of this user.");
  }

  /** confirms the participant can access resources from the given portal */
  public PortalWithPortalUser authParticipantToPortal(
      UUID participantUserId, String portalShortcode, EnvironmentName envName) {
    Optional<Portal> portalOpt = portalService.findOneByShortcode(portalShortcode);
    if (portalOpt.isPresent()) {
      Portal portal = portalOpt.get();
      Optional<PortalParticipantUser> ppUser =
          portalParticipantUserService.findOne(participantUserId, portalShortcode, envName);
      if (ppUser.isPresent()) {
        return new PortalWithPortalUser(portal, ppUser.get());
      }
    }
    throw new PermissionDeniedException(
        "User %s does not have permissions on portal %s, env %s"
            .formatted(participantUserId, portalShortcode, envName));
  }
}
