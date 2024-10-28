package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.participant.ProfileDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.NUMBER;

@Service
public class AgeTermParser extends SearchTermParser<AgeTerm> {
    private final ProfileDao profileDao;

    public AgeTermParser(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    @Override
    public AgeTerm parse(String term) {
        return new AgeTerm(profileDao);
    }

    @Override
    public String getTermName() {
        return "age";
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        return Map.of("age", SearchValueTypeDefinition.builder().type(NUMBER).build());
    }
}
