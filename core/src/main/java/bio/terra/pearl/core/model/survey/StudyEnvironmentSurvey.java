package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.publishing.VersionedEntityConfig;
import bio.terra.pearl.core.model.study.StudyEnvAttached;
import bio.terra.pearl.core.service.workflow.TaskAssignable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


/** Includes a survey in an environment and configures scheduling and who can take it */
@Getter
@Setter @SuperBuilder @NoArgsConstructor
public class StudyEnvironmentSurvey extends BaseEntity implements VersionedEntityConfig, StudyEnvAttached, TaskAssignable {
    private UUID studyEnvironmentId;
    private UUID surveyId;
    @Builder.Default
    private boolean active = true; // whether this represents a current configuration
    private Survey survey;
    private int surveyOrder; // what order the survey will be given in, compared to other surveys triggered at the same time
    @Override
    public Versioned versionedEntity() {
        return survey;
    }
    @Override
    public UUID versionedEntityId() { return surveyId; }
    @Override
    public void updateVersionedEntityId(UUID surveyId) {
        setSurveyId(surveyId);
    }


    @Override
    public String getStableId() {
        return survey.getStableId();
    }

    @Override
    public Integer getVersion() {
        return survey.getVersion();
    }

    @Override
    public RecurrenceType getRecurrenceType() {
        return survey.getRecurrenceType();
    }

    @Override
    public Integer getDaysAfterEligible() {
        return survey.getDaysAfterEligible();
    }

    @Override
    public Integer getRecurrenceIntervalDays() {
        return survey.getRecurrenceIntervalDays();
    }

    @Override
    public String getEligibilityRule() {
        return survey.getEligibilityRule();
    }

    @Override
    public Boolean isAutoAssign() {
        return survey.isAutoAssign();
    }

    @Override
    public Boolean isAutoUpdateTaskAssignments() {
        return survey.isAutoUpdateTaskAssignments();
    }

    @Override
    public Boolean isAssignToExistingEnrollees() {
        return survey.isAssignToExistingEnrollees();
    }
}
