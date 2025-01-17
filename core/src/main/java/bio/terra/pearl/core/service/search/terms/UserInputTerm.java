package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.List;
import java.util.Optional;

/**
 * Represents a search term that is based on user input, for example `John` in `{profile.givenName} = 'John'`.
 */
public class UserInputTerm extends SearchTerm {

    private final SearchValue searchValue;

    public UserInputTerm(SearchValue searchValue) {
        this.searchValue = searchValue;
    }

    @Override
    public SearchValue extract(EnrolleeSearchContext enrollee) {
        return searchValue;
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
    public List<Object> boundObjects() {
        switch (searchValue.getSearchValueType()) {
            case STRING -> {
                return List.of(searchValue.getStringValue());
            }
            case NUMBER -> {
                return List.of(searchValue.getNumberValue());
            }
            case BOOLEAN -> {
                return List.of(searchValue.getBooleanValue());
            }
            case INSTANT -> {
                return List.of(searchValue.getInstantValue());
            }
            case DATE -> {
                return List.of(searchValue.getDateValue());
            }
            default -> {
                throw new IllegalArgumentException("Unsupported term type: " + searchValue.getSearchValueType());
            }
        }
    }

    @Override
    public String termClause() {
        return "?";
    }

    @Override
    public SearchValueTypeDefinition type() {
        return SearchValueTypeDefinition.builder()
                .type(searchValue.getSearchValueType())
                .build();
    }

}
