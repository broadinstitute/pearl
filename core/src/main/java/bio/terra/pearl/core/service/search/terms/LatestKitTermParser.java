package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class LatestKitTermParser implements SearchTermParser<LatestKitTerm> {
    private final KitRequestDao kitRequestDao;
    public LatestKitTermParser(KitRequestDao kitRequestDao) {
        this.kitRequestDao = kitRequestDao;
    }

    @Override
    public LatestKitTerm parse(String term) {
        return new LatestKitTerm(kitRequestDao, getArgument(term));
    }

    @Override
    public String getTermName() {
        return "latestKit";
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        return addTermPrefix(LatestKitTerm.FIELDS);
    }
}
