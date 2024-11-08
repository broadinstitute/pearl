package bio.terra.pearl.core.model.fileupload;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * A file stored externally relating to a specific enrollee.
 * Could be uploaded by the participant or an admin.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParticipantFile extends BaseEntity {
    private String fileName;
    private String fileType; // e.g. pdf, docx, etc.

    private UUID externalFileId; // UUID of the file in the external storage system (e.g. GCS)

    private UUID creatingParticipantUserId; // the user who uploaded the file; could be proxy
    private UUID creatingAdminUserId; // for admin uploaded files

    private UUID enrolleeId;

    private String notes;
}
