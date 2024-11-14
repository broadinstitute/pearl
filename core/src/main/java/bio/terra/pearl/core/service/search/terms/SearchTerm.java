package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.service.search.EnrolleeSearchContext;
import bio.terra.pearl.core.service.search.sql.EnrolleeSearchQueryBuilder;
import org.jooq.Condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a term that can be used to search for enrollees. This can be anything from a field on the enrollee's profile
 * to a derived field like age. In addition, Functions are terms which modify other terms; for example, 'lower' or 'trim'.
 */
public abstract class SearchTerm {
    /**
     * Extract the term's value from the enrollee.
     */
    public abstract SearchValue extract(EnrolleeSearchContext enrollee);

    /**
     * Joins required to extract this term in a SQL search.
     */
    public abstract List<EnrolleeSearchQueryBuilder.JoinClause> requiredJoinClauses();

    /**
     * Select clauses required to extract this term in a SQL search.
     */
    public abstract List<EnrolleeSearchQueryBuilder.SelectClause> requiredSelectClauses();

    /**
     * Required where conditions - for example, if the term is an answer field, the survey and question stable ids
     * need to be checked before the term can be extracted.
     */
    public abstract Optional<Condition> requiredConditions();

    /**
     * The actual term clause to be used in the SQL query. For example, `profile.given_name` or `?` if it needs to be
     * bound and sanitized.
     */
    public abstract String termClause();

    /**
     * Bound objects to be used in the SQL query. For example, the value of the term if user inputted.
     */
    public abstract List<Object> boundObjects();

    /**
     * The type of value this term represents.
     */
    public abstract SearchValueTypeDefinition type();

    protected List<EnrolleeSearchQueryBuilder.JoinClause> joinClausesForStudy(String study) {
        validateStudyName(study);

        String enrolleeName = addStudySuffix("enrollee", study);
        String studyEnvName = addStudySuffix("study_environment", study);
        String studyName = addStudySuffix("study", study);

        return new ArrayList<>(List.of(
                new EnrolleeSearchQueryBuilder.JoinClause("inner", "enrollee", enrolleeName, "profile.id = %s.profile_id".formatted(enrolleeName)),
                new EnrolleeSearchQueryBuilder.JoinClause("inner", "study_environment", studyEnvName, "%s.study_environment_id = %s.id".formatted(enrolleeName, studyEnvName)),
                new EnrolleeSearchQueryBuilder.JoinClause("inner", "study", studyName, "%s.study_id = %s.id AND %s.name = '%s'".formatted(studyEnvName, studyName, studyName, study))
        ));
    }

    protected String addStudySuffix(String name, String study) {
        return name + "_" + study;
    }

    private void validateStudyName(String study) {
        if (!study.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid study name: " + study);
        }
    }
}
