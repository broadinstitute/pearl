package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for parsing search terms.
 */
public abstract class SearchTermParser<T extends SearchTerm> {
    public T parseVariable(String variable) {
        String variableNoBraces = stripBraces(variable);

        List<String> splitModelName = List.of(variableNoBraces.split("\\.", 2));

        if (!splitModelName.getFirst().equals(this.getTermName())) {
            throw new IllegalArgumentException("Variable does not match term parser: " + variable);
        }

        if (splitModelName.size() == 1) {
            return this.parse("");
        } else {
            return this.parse(splitModelName.get(1));
        }
    }

    /**
     * Parse the term string into a search term.
     */
    protected abstract T parse(String arguments);

    /**
     * Get the facets that can be used in a search expression for the given study environment.
     */
    public abstract Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId);

    /**
     * The name of the term. E.g., AgeTermParser would return "age".
     */
    public abstract String getTermName();

    /**
     * Check if the variable matches this term parser.
     */
    public Boolean match(String variable) {
        variable = stripBraces(variable);
        return variable.equals(this.getTermName())
                || variable.startsWith(this.getTermName() + ".");
    };

    /**
     * Splits variable into arguments, e.g. "answer.surveyStableId.questionStableId" -> ["surveyStableId", "questionStableId"]
     */
    protected List<String> splitArguments(String arguments) {
        return List.of(arguments.split("\\."));
    }

    /**
     * Splits variable into arguments based on limit
     * e.g. with limit 2 "answer.arg1.arg2.arg3" -> ["arg1", "arg2.arg3"]
     */
    protected List<String> splitArguments(String arguments, int numArguments) {
        return List.of(arguments.split("\\.", numArguments + 1));
    }

    protected String stripBraces(String variable) {
        if (variable.startsWith("{") && variable.endsWith("}")) {
            return variable.substring(1, variable.length() - 1);
        }
        return variable;
    }

    /**
     * Add the term prefix to the facets. E.g., "givenName" -> "profile.givenName"
     */
    protected Map<String, SearchValueTypeDefinition> addTermPrefix(Map<String, SearchValueTypeDefinition> facets) {
        Map<String, SearchValueTypeDefinition> newFacets = new HashMap<>();
        for (Map.Entry<String, SearchValueTypeDefinition> entry : facets.entrySet()) {
            newFacets.put(this.getTermName() + "." + entry.getKey(), entry.getValue());
        }
        return newFacets;
    }

}
