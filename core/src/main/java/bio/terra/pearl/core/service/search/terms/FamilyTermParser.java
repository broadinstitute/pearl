package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.FamilyDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class FamilyTermParser implements SearchTermParser<FamilyTerm> {
    private final FamilyDao familyDao;
    public FamilyTermParser(FamilyDao familyDao) {
        this.familyDao = familyDao;
    }

    @Override
    public FamilyTerm parse(String term) {
        return new FamilyTerm(familyDao, getArgument(term));
    }

    @Override
    public String getTermName() {
        return "family";
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        return addTermPrefix(FamilyTerm.FIELDS);
    }
}
