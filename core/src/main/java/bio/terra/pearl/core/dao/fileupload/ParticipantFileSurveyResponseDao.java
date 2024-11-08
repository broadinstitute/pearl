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

    public void deleteByEnrolleeId(UUID enrolleeId) {
        jdbi.withHandle(handle ->
                handle.createUpdate("""
                                    delete from participant_file_survey_response file_response
                                    where id IN (
                                        select inner_file_response.id from participant_file_survey_response inner_file_response 
                                        inner join participant_file inner_file on inner_file_response.participant_file_id = inner_file.id 
                                        where inner_file.enrollee_id = :enrolleeId
                                    );
                                """)
                        .bind("enrolleeId", enrolleeId)
                        .execute()
        );
    }
}
