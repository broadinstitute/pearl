package bio.terra.pearl.populate.dto.fileupload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ParticipantFileReferenceDto {
    private String fileName;
    private Integer fileVersion;
}
