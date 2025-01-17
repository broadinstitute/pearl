package bio.terra.pearl.api.admin.controller.portal;

import bio.terra.pearl.api.admin.api.PortalApi;
import bio.terra.pearl.api.admin.models.dto.PortalShallowDto;
import bio.terra.pearl.api.admin.service.auth.AuthUtilService;
import bio.terra.pearl.api.admin.service.auth.context.OperatorAuthContext;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.api.admin.service.portal.PortalExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PortalController implements PortalApi {
  private PortalExtService portalExtService;
  private ObjectMapper objectMapper;
  private AuthUtilService requestService;
  private final HttpServletRequest request;

  public PortalController(
      PortalExtService portalExtService,
      ObjectMapper objectMapper,
      AuthUtilService requestService,
      HttpServletRequest request) {
    this.portalExtService = portalExtService;
    this.objectMapper = objectMapper;
    this.requestService = requestService;
    this.request = request;
  }

  @Override
  public ResponseEntity<Object> get(String portalShortcode, String language) {
    AdminUser operator = requestService.requireAdminUser(request);
    Portal portal =
        portalExtService.fullLoad(PortalAuthContext.of(operator, portalShortcode), language);
    return ResponseEntity.ok(portal);
  }

  @Override
  public ResponseEntity<Object> getAll() {
    AdminUser operator = requestService.requireAdminUser(request);
    List<Portal> portals = portalExtService.getAll(OperatorAuthContext.of(operator));
    List<PortalShallowDto> portalDtos =
        portals.stream()
            .map(portal -> objectMapper.convertValue(portal, PortalShallowDto.class))
            .collect(Collectors.toList());
    return ResponseEntity.ok(portalDtos);
  }
}
