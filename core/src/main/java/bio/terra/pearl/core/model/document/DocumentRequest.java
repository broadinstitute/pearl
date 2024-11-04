package bio.terra.pearl.core.model.document;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.workflow.RecurrenceType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class DocumentRequest extends BaseEntity implements Versioned {
    private String stableId;

    @Builder.Default
    private int version = 1;
    private Integer publishedVersion;

    private UUID portalId;

    // document config
    private String documentName;
    private String blurb;

    private String allowedFileTypes; // comma separated list of file extensions
    private Integer maxFileSize; // in bytes

    private Boolean multipleFilesAllowed;

    // task assignment config
    private RecurrenceType recurrenceType;

    private Integer daysAfterEligible;
    private Integer recurrenceIntervalDays;

    private String eligibilityRule;

    private boolean autoAssign;
    private boolean autoUpdateTaskAssignments;
    private boolean assignToExistingEnrollees;
}

