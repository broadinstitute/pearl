package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.i18n.LanguageTextFactory;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.model.portal.Portal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class LanguageTextServiceTests extends BaseSpringBootTest  {

    @Autowired
    private LanguageTextService languageTextService;

    @Autowired
    private LanguageTextFactory languageTextFactory;

    @Autowired
    private PortalFactory portalFactory;

    @Test
    @Transactional
    public void testFindSystemTextByKeyAndLanguage(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        languageTextFactory.buildPersisted(testName, "testLogin", "fr");
        languageTextFactory.buildPersisted(testName, "testLogin", "es");

        Optional<LanguageText> frenchLoginText = languageTextService.findSystemTextByKeyAndLanguage(testName + "testLogin", "fr");
        assertThat(frenchLoginText.isPresent(), equalTo(true));
        assertThat(frenchLoginText.get().getText(), equalTo(testName + " text"));

        Optional<LanguageText> missingText = languageTextService.findSystemTextByKeyAndLanguage("doesNotExist", "fr");
        assertThat(missingText.isPresent(), equalTo(false));

        Portal portal = portalFactory.buildPersisted(testName);

        languageTextService.create(
                LanguageText
                        .builder()
                        .text("SHOULD NOT RETURN OVERRIDE")
                        .language("fr")
                        .keyName("testLogin").build());

        Optional<LanguageText> portalFrenchLoginText = languageTextService.findSystemTextByKeyAndLanguage(testName + "testLogin", "fr");

        assertThat(portalFrenchLoginText.isPresent(), equalTo(true));
        assertThat(portalFrenchLoginText.get().getText(), equalTo(testName + " text"));
    }

    @Test
    @Transactional
    public void testGetLanguageTextMapForLanguage(TestInfo testInfo) {
        String testName = getTestName(testInfo);
        languageTextFactory.buildPersisted(testName, "login", "testLang");
        languageTextFactory.buildPersisted(testName, "logout", "testLang");
        languageTextFactory.buildPersisted(testName, "logout", "otherTestLang");

        Map<String, String> langTexts = languageTextService.getLanguageTextMapForLanguage(null, "testLang");

        assertThat(langTexts, equalTo(Map.of(
            testName + "login", testName + " text",
            testName + "logout", testName + " text"
        )));
    }

}
