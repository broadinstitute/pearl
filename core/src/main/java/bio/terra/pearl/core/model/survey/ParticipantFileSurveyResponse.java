package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ParticipantFileSurveyResponse extends BaseEntity {
    private UUID participantFileId;
    private UUID surveyResponseId;

    public UUID creatingParticipantUserId;
    public UUID creatingAdminUserId;

    public void setResponsibleUser(ResponsibleEntity responsibleEntity) {
        if (responsibleEntity == null) {
            return;
        }
        if (responsibleEntity.getParticipantUser() != null) {
            this.creatingParticipantUserId = responsibleEntity.getParticipantUser().getId();
        }
        if (responsibleEntity.getAdminUser() != null) {
            this.creatingAdminUserId = responsibleEntity.getAdminUser().getId();
        }
    }
}
