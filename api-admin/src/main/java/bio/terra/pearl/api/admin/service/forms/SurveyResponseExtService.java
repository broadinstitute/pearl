package bio.terra.pearl.api.admin.service.forms;

import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.EnforcePortalEnrolleePermission;
import bio.terra.pearl.api.admin.service.auth.context.PortalEnrolleeAuthContext;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.SurveyResponseWithJustification;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SurveyResponseExtService {
  private final AuthUtilService authUtilService;
  private final PortalParticipantUserService portalParticipantUserService;
  private final SurveyResponseService surveyResponseService;

  public SurveyResponseExtService(
      AuthUtilService authUtilService,
      PortalParticipantUserService portalParticipantUserService,
      SurveyResponseService surveyResponseService) {
    this.authUtilService = authUtilService;
    this.portalParticipantUserService = portalParticipantUserService;
    this.surveyResponseService = surveyResponseService;
  }

  @EnforcePortalEnrolleePermission(permission = "participant_data_edit")
  public HubResponse updateResponse(
      PortalEnrolleeAuthContext authContext,
      SurveyResponseWithJustification responseDto,
      UUID taskId) {
    Portal portal = authContext.getPortal();
    PortalParticipantUser ppUser =
        portalParticipantUserService.findForEnrollee(authContext.getEnrollee());
    String justification = responseDto.getJustification();

    HubResponse result =
        surveyResponseService.updateResponse(
            responseDto,
            new ResponsibleEntity(authContext.getOperator()),
            justification,
            ppUser,
            authContext.getEnrollee(),
            taskId,
            portal.getId());
    return result;
  }
}
