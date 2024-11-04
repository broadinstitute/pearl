package bio.terra.pearl.core.model.document;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.workflow.RecurrenceType;
import bio.terra.pearl.core.service.workflow.TaskConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DocumentRequest extends BaseEntity implements TaskConfig {
    private String stableId;
    private Integer version;

    private UUID studyEnvironmentId;

    // document config
    private String documentName;
    private String blurb;

    private String allowedFileTypes; // comma separated list of file extensions
    private Integer maxFileSize; // in bytes

    private Boolean multipleFilesAllowed;

    // task assignment config
    private Integer taskOrder;
    private boolean required;

    private RecurrenceType recurrenceType;

    private Integer daysAfterEligible;
    private Integer recurrenceIntervalDays;

    private String eligibilityRule;

    private boolean autoAssign;
    private boolean autoUpdateTaskAssignments;
    private boolean assignToExistingEnrollees;
}

