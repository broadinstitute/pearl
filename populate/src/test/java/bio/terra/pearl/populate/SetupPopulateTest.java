package bio.terra.pearl.populate;

import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.service.admin.AdminUserService;
import bio.terra.pearl.core.service.i18n.LanguageTextService;
import bio.terra.pearl.populate.service.BaseSeedPopulator;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests that we can populate a clean environment with admin users and environments
 * This also indirectly tests the PopulateDispatcher
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SetupPopulateTest extends BaseSpringBootTest {
    @Autowired
    BaseSeedPopulator baseSeedPopulator;
    @Autowired
    AdminUserService adminUserService;
    @Autowired
    LanguageTextService languageTextService;

    @Test
    @Transactional
    public void testSetup(TestInfo info) throws IOException {
        adminUserService.bulkDelete(adminUserService.findAll(), getAuditInfo(info));
        BaseSeedPopulator.SetupStats setupStats = baseSeedPopulator.populate("");
        Assertions.assertEquals(BaseSeedPopulator.ADMIN_USERS_TO_POPULATE.size(), setupStats.getNumAdminUsers());
    }

    @Test
    @Transactional
    public void testPopulateLanguageTexts(TestInfo info) {
        baseSeedPopulator.populateLanguageTexts();
        List<LanguageText> languageTexts = languageTextService.findAll();
        Map<String, List<LanguageText>> languageTextsByLanguageCode = languageTexts.stream()
            .collect(java.util.stream.Collectors.groupingBy(LanguageText::getLanguage));

        Assertions.assertEquals(BaseSeedPopulator.LANGUAGE_TEXTS_TO_POPULATE.size(), languageTextsByLanguageCode.size());
    }
}
