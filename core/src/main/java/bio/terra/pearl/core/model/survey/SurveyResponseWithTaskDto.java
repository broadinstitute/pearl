package bio.terra.pearl.core.model.survey;

import bio.terra.pearl.core.model.workflow.ParticipantTask;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SurveyResponseWithTaskDto extends SurveyResponse{
    private ParticipantTask task;

    public SurveyResponseWithTaskDto(SurveyResponse surveyResponse, ParticipantTask task) {
        BeanUtils.copyProperties(surveyResponse, this);
        this.task = task;
    }
}
