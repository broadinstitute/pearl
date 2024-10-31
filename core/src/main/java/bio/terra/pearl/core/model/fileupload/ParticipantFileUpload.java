package bio.terra.pearl.core.model.fileupload;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParticipantFileUpload extends BaseEntity {
    private String fileName;
    private String fileType; // e.g. pdf, docx, etc.

    private UUID uploadedFileId; // files are uploaded as UUIDs

    private UUID creatingPortalParticipantUserId; // the user who uploaded the file; could be proxy
    private UUID creatingAdminUserId; // for admin uploaded files

    private UUID portalParticipantUserId;

    private String notes;
}
