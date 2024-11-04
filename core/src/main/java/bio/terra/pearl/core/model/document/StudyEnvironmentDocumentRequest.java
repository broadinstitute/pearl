package bio.terra.pearl.core.model.document;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.publishing.VersionedEntityConfig;
import bio.terra.pearl.core.model.study.StudyEnvAttached;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class StudyEnvironmentDocumentRequest extends BaseEntity implements VersionedEntityConfig, StudyEnvAttached {

    private UUID studyEnvironmentId;
    private UUID documentRequestId;

    private DocumentRequest documentRequest;

    @Builder.Default
    private boolean active = true;
    private int taskOrder;

    @Override
    public Versioned versionedEntity() {
        return documentRequest;
    }
    @Override
    public UUID versionedEntityId() { return documentRequestId; }
    @Override
    public void updateVersionedEntityId(UUID documentRequestId) {
        setDocumentRequestId(documentRequestId);
    }

}
