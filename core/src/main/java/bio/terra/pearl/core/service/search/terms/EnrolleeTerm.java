package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static bio.terra.pearl.core.dao.BaseJdbiDao.toSnakeCase;
import static bio.terra.pearl.core.service.search.terms.SearchValue.SearchValueType.*;

/**
 * Allows searching on basic properties of the enrollee, e.g. "consented"
 */
public class EnrolleeTerm extends SearchTerm {

    private final String field;

    public EnrolleeTerm(String field) {
        if (!FIELDS.containsKey(field)) {
            throw new IllegalArgumentException("Invalid field: " + field);
        }

        this.field = field;
    }


    @Override
    public SearchValue extract(EnrolleeSearchContext context) {
        return SearchValue.ofNestedProperty(context.getEnrollee(), field, FIELDS.get(field).getType());
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses() {
        return List.of();
    }

    @Override
    public List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses() {
        return List.of();
    }

    @Override
    public Optional<Condition> requiredConditions() {
        return Optional.empty();
    }

    @Override
    public String termClause() {
        return "enrollee." + toSnakeCase(field);
    }

    @Override
    public List<Object> boundObjects() {
        return List.of();
    }

    @Override
    public SearchValueTypeDefinition type() {
        return FIELDS.get(field);
    }

    public static final Map<String, SearchValueTypeDefinition> FIELDS = Map.ofEntries(
            Map.entry("shortcode", SearchValueTypeDefinition.builder().type(STRING).build()),
            Map.entry("subject", SearchValueTypeDefinition.builder().type(BOOLEAN).build()),
            Map.entry("consented", SearchValueTypeDefinition.builder().type(BOOLEAN).build()),
            Map.entry("createdAt", SearchValueTypeDefinition.builder().type(INSTANT).build()));
}
