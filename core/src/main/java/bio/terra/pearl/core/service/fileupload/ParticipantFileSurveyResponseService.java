package bio.terra.pearl.core.service.fileupload;

import bio.terra.pearl.core.dao.fileupload.ParticipantFileSurveyResponseDao;
import bio.terra.pearl.core.model.survey.ParticipantFileSurveyResponse;
import bio.terra.pearl.core.service.ImmutableEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ParticipantFileSurveyResponseService extends ImmutableEntityService<ParticipantFileSurveyResponse, ParticipantFileSurveyResponseDao> {
    public ParticipantFileSurveyResponseService(ParticipantFileSurveyResponseDao dao) {
        super(dao);
    }

    public List<ParticipantFileSurveyResponse> findBySurveyResponseId(UUID surveyResponseId) {
        return dao.findBySurveyResponseId(surveyResponseId);
    }
}
