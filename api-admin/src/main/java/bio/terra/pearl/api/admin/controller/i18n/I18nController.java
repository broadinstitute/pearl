package bio.terra.pearl.api.admin.controller.i18n;

import bio.terra.pearl.api.admin.api.I18nApi;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.i18n.LanguageTextService;
import bio.terra.pearl.core.service.portal.PortalService;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class I18nController implements I18nApi {
  private final LanguageTextService languageTextService;
  private final PortalService portalService;

  public I18nController(LanguageTextService languageTextService, PortalService portalService) {
    this.languageTextService = languageTextService;
    this.portalService = portalService;
  }

  @Override
  public ResponseEntity<Object> listLanguageTexts(String language, String portalShortcode) {
    Optional<Portal> portal = portalService.findOneByShortcode(portalShortcode);

    //default to English if no language is provided
    String lang = Objects.requireNonNullElse(language, "en");

    HashMap<String, String> languageTexts;
    //if the portal is not found or not specified, at least return the global/system language texts
    //it's possible to need language texts without a loaded portal context in the admin tool
    if (portal.isEmpty()) {
      languageTexts = languageTextService.getSystemLanguageTextMap(lang);
    } else {
      languageTexts = languageTextService.getLanguageTextMapForLanguage(portal.get().getId(), lang);
    }
    return ResponseEntity.ok(languageTexts);
  }
}
