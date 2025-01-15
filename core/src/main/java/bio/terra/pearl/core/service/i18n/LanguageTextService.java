package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.dao.i18n.LanguageTextDao;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LanguageTextService extends CrudService<LanguageText, LanguageTextDao> {

    private LanguageTextDao languageTextDao;

    public LanguageTextService(LanguageTextDao languageTextDao) {
        super(languageTextDao);
        this.languageTextDao = languageTextDao;
    }

    @Cacheable(value = "languageTexts", key = "#language")
    public HashMap<String, String> getLanguageTextMapForLanguage(UUID portalEnvId, String language) {
        List<LanguageText> languageTexts = languageTextDao.findWithOverridesByPortalEnvId(portalEnvId, language);

        HashMap<String, String> languageTextMap = new HashMap<>();
        for (LanguageText languageText : languageTexts) {
            languageTextMap.put(languageText.getKeyName(), languageText.getText());
        }

        return languageTextMap;
    }

    public void deleteByPortalId(UUID portalId) {
        languageTextDao.deleteByPortalId(portalId);
    }

    public Optional<LanguageText> findByKeyNameAndLanguage(String keyName, String language) {
        return languageTextDao.findByKeyNameAndLanguage(keyName, language);
    }

    public void deleteByLocalSite(UUID localSiteId, Set<CascadeProperty> cascades) {
        languageTextDao.deleteByLocalSite(localSiteId);
    }
}
