package bio.terra.pearl.core.dao.fileupload;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.survey.ParticipantFileSurveyResponse;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ParticipantFileSurveyResponseDao extends BaseJdbiDao<ParticipantFileSurveyResponse> {
    public ParticipantFileSurveyResponseDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ParticipantFileSurveyResponse> getClazz() {
        return ParticipantFileSurveyResponse.class;
    }

    public List<ParticipantFileSurveyResponse> findBySurveyResponseId(UUID surveyResponseId) {
        return findAllByProperty("survey_response_id", surveyResponseId);
    }
}
