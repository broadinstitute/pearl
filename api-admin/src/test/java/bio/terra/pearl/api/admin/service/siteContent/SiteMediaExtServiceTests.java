package bio.terra.pearl.api.admin.service.siteContent;

import bio.terra.pearl.api.admin.BaseSpringBootTest;
import bio.terra.pearl.api.admin.service.auth.context.PortalAuthContext;
import bio.terra.pearl.core.factory.admin.AdminUserFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.exception.NotFoundException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SiteMediaExtServiceTests extends BaseSpringBootTest {
  @Autowired private AdminUserFactory adminUserFactory;
  @Autowired private PortalFactory portalFactory;
  @Autowired private SiteMediaExtService siteMediaExtService;

  @Test
  @Transactional
  public void imageListAuthsToPortal(TestInfo info) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), false);
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          siteMediaExtService.list(PortalAuthContext.of(operator, portal.getShortcode()));
        });
  }

  @Test
  @Transactional
  public void uploadAuthsToPortal(TestInfo info) {
    AdminUser operator = adminUserFactory.buildPersisted(getTestName(info), false);
    Portal portal = portalFactory.buildPersisted(getTestName(info));
    Assertions.assertThrows(
        NotFoundException.class,
        () -> {
          siteMediaExtService.upload(
              PortalAuthContext.of(operator, portal.getShortcode()),
              "blah",
              "blah".getBytes(StandardCharsets.UTF_8));
        });
  }
}
