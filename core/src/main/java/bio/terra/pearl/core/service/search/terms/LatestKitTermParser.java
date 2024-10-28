package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class LatestKitTermParser extends SearchTermParser<LatestKitTerm> {
    private final KitRequestDao kitRequestDao;
    public LatestKitTermParser(KitRequestDao kitRequestDao) {
        this.kitRequestDao = kitRequestDao;
    }

    @Override
    public LatestKitTerm parse(String field) {
        return new LatestKitTerm(kitRequestDao, field);
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
