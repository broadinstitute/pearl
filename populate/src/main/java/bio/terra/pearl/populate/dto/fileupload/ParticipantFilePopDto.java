package bio.terra.pearl.populate.dto.fileupload;

import bio.terra.pearl.core.model.file.ParticipantFile;
import bio.terra.pearl.populate.dto.FilePopulatable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParticipantFilePopDto extends ParticipantFile implements FilePopulatable {
    String populateFileName;

    String fileContent;
}
