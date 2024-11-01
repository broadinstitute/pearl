package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.model.survey.RecurrenceType;

import java.util.UUID;

public interface TaskAssignable {

    String getStableId();
    Integer getVersion();

    RecurrenceType getRecurrenceType();
    Integer getDaysAfterEligible();
    Integer getRecurrenceIntervalDays();
    UUID getStudyEnvironmentId();
    String getEligibilityRule();
    Boolean isAutoAssign();
    Boolean isAutoUpdateTaskAssignments();
    Boolean isAssignToExistingEnrollees();
}
