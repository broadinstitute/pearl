package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class EnrolleeTermParser extends SearchTermParser<EnrolleeTerm> {
    @Override
    public EnrolleeTerm parse(String field) {
        return new EnrolleeTerm(field);
    }

    @Override
    public String getTermName() {
        return "enrollee";
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        return addTermPrefix(EnrolleeTerm.FIELDS);
    }
}
