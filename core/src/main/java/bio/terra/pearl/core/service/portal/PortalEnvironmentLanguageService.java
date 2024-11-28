package bio.terra.pearl.core.service.portal;

import bio.terra.pearl.core.dao.portal.PortalEnvironmentLanguageDao;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentLanguage;
import bio.terra.pearl.core.model.publishing.ListChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.ImmutableEntityService;
import bio.terra.pearl.core.service.publishing.PortalEnvPublishable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class PortalEnvironmentLanguageService extends ImmutableEntityService<PortalEnvironmentLanguage, PortalEnvironmentLanguageDao>
 implements PortalEnvPublishable {

    public PortalEnvironmentLanguageService(PortalEnvironmentLanguageDao portalEnvironmentLanguageDao) {
        super(portalEnvironmentLanguageDao);
    }

    public List<PortalEnvironmentLanguage> findByPortalEnvId(UUID portalId) {
        return dao.findByPortalEnvId(portalId);
    }

    /** for now, just do a hard delete/recreate */
    public List<PortalEnvironmentLanguage> setPortalEnvLanguages(UUID portalEnvId, List<PortalEnvironmentLanguage> languages) {
        dao.deleteByPortalEnvId(portalEnvId);
        return languages.stream().map(language -> {
            language.setPortalEnvironmentId(portalEnvId);
            return dao.create(language.cleanForCopying());
        }).toList();
    }

    @Override
    public void loadForPublishing(PortalEnvironment portalEnv) {
        portalEnv.setSupportedLanguages(findByPortalEnvId(portalEnv.getId()));
    }

    @Override
    public void updateDiff(PortalEnvironmentChange change, PortalEnvironment sourceEnv, PortalEnvironment destEnv) {
        List<PortalEnvironmentLanguage> unmatchedDestLangs = new ArrayList<>(destEnv.getSupportedLanguages());
        List<PortalEnvironmentLanguage> addedLangs = new ArrayList<>();
        for (PortalEnvironmentLanguage sourceLang : sourceEnv.getSupportedLanguages()) {
            PortalEnvironmentLanguage matchedLang = unmatchedDestLangs.stream().filter(
                            destLang -> destLang.getLanguageCode().equals(sourceLang.getLanguageCode()) && destLang.getLanguageName().equals(sourceLang.getLanguageName()))
                    .findAny().orElse(null);
            if (matchedLang == null) {
                addedLangs.add(sourceLang);
            } else {
                unmatchedDestLangs.remove(matchedLang);
            }
        }
        change.setLanguageChanges(new ListChange<>(addedLangs, unmatchedDestLangs, Collections.emptyList()));
    }

    @Override
    public void applyDiff(PortalEnvironmentChange change, PortalEnvironment destEnv) {
        ListChange<PortalEnvironmentLanguage, Object> languageChanges = change.getLanguageChanges();
        for (PortalEnvironmentLanguage language : languageChanges.addedItems()) {
            language.cleanForCopying();
            language.setPortalEnvironmentId(destEnv.getId());
            create(language);
        }
        for (PortalEnvironmentLanguage language : languageChanges.removedItems()) {
            delete(language.getId(), CascadeProperty.EMPTY_SET);
        }
        // we don't have a concept of a "change" language -- it's all create/delete, so we don't need to handle that case
    }
}
