package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.survey.RecurrenceType;

import java.util.UUID;

public interface TaskConfig {
    String getStableId();
    Integer getVersion();

    UUID getStudyEnvironmentId();

    RecurrenceType getRecurrenceType();
    Integer getDaysAfterEligible();
    Integer getRecurrenceIntervalDays();

    String getEligibilityRule();

    Boolean isAutoAssign();
    Boolean isAutoUpdateTaskAssignments();
    Boolean isAssignToExistingEnrollees();
}
