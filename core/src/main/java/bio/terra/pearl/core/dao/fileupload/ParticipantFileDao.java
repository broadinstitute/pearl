package bio.terra.pearl.core.dao.fileupload;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.fileupload.ParticipantFile;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ParticipantFileDao extends BaseJdbiDao<ParticipantFile> {
    public ParticipantFileDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<ParticipantFile> getClazz() {
        return ParticipantFile.class;
    }

    public List<ParticipantFile> findBySurveyResponseId(UUID surveyResponseId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select file.* from %s file
                                inner join participant_file_survey_response file_response on file.id = file_response.participant_file_id
                                where file_response.survey_response_id = :surveyResponseId
                                """.formatted(tableName))
                        .bind("surveyResponseId", surveyResponseId)
                        .mapTo(clazz)
                        .stream()
                        .toList()
        );
    }
}