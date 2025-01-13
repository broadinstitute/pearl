package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.AnswerFormat;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.file.ParticipantFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnswerService extends CrudService<Answer, AnswerDao> {
    private final ParticipantFileService participantFileService;

    public AnswerService(AnswerDao dao, ParticipantFileService participantFileService) {
        super(dao);
        this.participantFileService = participantFileService;
    }

    public Optional<Answer> findForQuestion(UUID surveyResponseId, String questionStableId) {
        return dao.findByResponseQuestion(surveyResponseId, questionStableId);
    }

    public List<Answer> findByResponse(UUID surveyResponseId) {
        return dao.findByResponse(surveyResponseId);
    }

    public List<Answer> findByResponseAndQuestions(UUID surveyResponseId, List<String> questionStableIds) {
        return dao.findByResponseAndQuestions(surveyResponseId, questionStableIds);
    }

    public List<Answer> findByEnrollee(UUID enrolleeID) {
        return dao.findByEnrollee(enrolleeID);
    }

    public List<Answer> findByEnrolleeAndSurvey(UUID enrolleeID, String surveyStableId) {
        return dao.findByEnrolleeAndSurvey(enrolleeID, surveyStableId);
    }

    public Optional<Answer> findForEnrolleeByQuestion(UUID enrolleeID, String surveyStableId, String questionStableId) {
        return dao.findForEnrolleeByQuestion(enrolleeID, surveyStableId, questionStableId);
    }

    public void deleteByResponseId(UUID responseId) {
        dao.deleteByResponseId(responseId);
    }

    @Override
    @Transactional
    public Answer create(Answer answer) {
        validateAnswer(answer);
        return super.create(answer);
    }

    @Override
    @Transactional
    public Answer update(Answer answer) {
        validateAnswer(answer);
        return super.update(answer);
    }

    private void validateAnswer(Answer answer) {
        answer.inferTypeIfMissing();
        if (answer.getFormat() == null) {
            answer.setFormat(AnswerFormat.NONE);
        }

        if (answer.getFormat().equals(AnswerFormat.FILE_NAME)) {
            participantFileService.findByEnrolleeIdAndFileName(answer.getEnrolleeId(), answer.getStringValue())
                    .orElseThrow(() -> new IllegalArgumentException("File not found for answer"));
        }
    }
}
