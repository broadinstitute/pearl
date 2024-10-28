package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for parsing search terms.
 */
public interface SearchTermParser<T extends SearchTerm> {
    /**
     * Parse the term string into a search term.
     */
    T parse(String variable);

    /**
     * Get the facets that can be used in a search expression for the given study environment.
     */
    Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId);

    /**
     * The name of the term. E.g., AgeTermParser would return "age".
     */
    String getTermName();

    /**
     * Check if the variable matches this term parser.
     */
    default Boolean match(String variable) {
        variable = stripBraces(variable);
        return variable.equals(this.getTermName()) || variable.startsWith(this.getTermName() + ".");
    };

    /**
     * Splits variable into arguments, e.g. "answer.surveyStableId.questionStableId" -> ["surveyStableId", "questionStableId"]
     */
    default List<String> getArguments(String variable) {
        return List.of(getArgument(variable).split("\\."));
    }

    /**
     * Splits variable into arguments based on limit
     * e.g. with limit 2 "answer.arg1.arg2.arg3" -> ["arg1", "arg2.arg3"]
     */
    default List<String> getArguments(String variable, int limit) {
        return List.of(getArgument(variable).split("\\.", limit + 1));
    }

    /**
     * Get the argument portion of the variable, e.g. "answer.surveyStableId.questionStableId" -> "surveyStableId.questionStableId"
     */
    default String getArgument(String variable) {
        variable = stripBraces(variable);

        List<String> split = List.of(variable.split("\\.", 2));
        if (split.getFirst().equals(this.getTermName())) {
            return split.get(1);
        }
        return variable;
    }

    default String stripBraces(String variable) {
        if (variable.startsWith("{") && variable.endsWith("}")) {
            return variable.substring(1, variable.length() - 1);
        }
        return variable;
    }

    /**
     * Add the term prefix to the facets. E.g., "givenName" -> "profile.givenName"
     */
    default Map<String, SearchValueTypeDefinition> addTermPrefix(Map<String, SearchValueTypeDefinition> facets) {
        Map<String, SearchValueTypeDefinition> newFacets = new HashMap<>();
        for (Map.Entry<String, SearchValueTypeDefinition> entry : facets.entrySet()) {
            newFacets.put(this.getTermName() + "." + entry.getKey(), entry.getValue());
        }
        return newFacets;
    }

}
