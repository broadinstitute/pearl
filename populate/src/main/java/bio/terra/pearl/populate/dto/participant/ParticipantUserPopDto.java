package bio.terra.pearl.populate.dto.participant;

import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.populate.dto.TimeShiftable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParticipantUserPopDto extends ParticipantUser {
    private Integer lastLoginHoursAgo;
    /** for cases where we want to generate a unique user on every populate to avoid b2c conflicts */
    private String usernamePrefix;
}
