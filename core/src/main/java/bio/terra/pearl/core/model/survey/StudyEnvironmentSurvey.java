package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.publishing.VersionedEntityConfig;
import bio.terra.pearl.core.model.study.StudyEnvAttached;
import bio.terra.pearl.core.model.workflow.RecurrenceType;
import bio.terra.pearl.core.service.workflow.TaskConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


/** Includes a survey in an environment and configures scheduling and who can take it */
@Getter
@Setter @SuperBuilder @NoArgsConstructor
public class StudyEnvironmentSurvey extends BaseEntity implements VersionedEntityConfig, StudyEnvAttached, TaskConfig {
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
        if (survey == null) {
            return null;
        }
        return survey.getStableId();
    }

    @Override
    public Integer getVersion() {
        if (survey == null) {
            return null;
        }
        return survey.getVersion();
    }

    @Override
    public RecurrenceType getRecurrenceType() {
        if (survey == null) {
            return null;
        }
        return survey.getRecurrenceType();
    }

    @Override
    public Integer getDaysAfterEligible() {
        if (survey == null) {
            return null;
        }
        return survey.getDaysAfterEligible();
    }

    @Override
    public Integer getRecurrenceIntervalDays() {
        if (survey == null) {
            return null;
        }
        return survey.getRecurrenceIntervalDays();
    }

    @Override
    public String getEligibilityRule() {
        if (survey == null) {
            return null;
        }
        return survey.getEligibilityRule();
    }

    @Override
    public Boolean isAutoAssign() {
        if (survey == null) {
            return null;
        }
        return survey.isAutoAssign();
    }

    @Override
    public Boolean isAutoUpdateTaskAssignments() {
        if (survey == null) {
            return null;
        }
        return survey.isAutoUpdateTaskAssignments();
    }

    @Override
    public Boolean isAssignToExistingEnrollees() {
        if (survey == null) {
            return null;
        }
        return survey.isAssignToExistingEnrollees();
    }

    @Override
    public Boolean isRequired() {
        if (survey == null) {
            return null;
        }
        return survey.isRequired();
    }

    @Override
    public String getName() {
        if (survey == null) {
            return null;
        }
        return survey.getName();
    }

    @Override
    public Integer getTaskOrder() {
        return surveyOrder;
    }
}
