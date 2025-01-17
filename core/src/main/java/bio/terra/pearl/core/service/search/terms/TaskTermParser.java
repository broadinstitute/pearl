package bio.terra.pearl.core.service.search.terms;

import bio.terra.pearl.core.dao.workflow.ParticipantTaskDao;
import bio.terra.pearl.core.model.search.SearchValueTypeDefinition;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.survey.SurveyService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskTermParser extends SearchTermParser<TaskTerm> {
    private final ParticipantTaskDao participantTaskDao;
    private final SurveyService surveyService;

    public TaskTermParser(ParticipantTaskDao participantTaskDao, SurveyService surveyService) {
        this.participantTaskDao = participantTaskDao;
        this.surveyService = surveyService;
    }

    @Override
    public TaskTerm parse(String term) {
        List<String> arguments = splitArguments(term, 2);

        if (arguments.size() != 2) {
            throw new IllegalArgumentException("Task term must have two arguments: {task.taskStableId.field}. Instead, got: " + term);
        }

        return new TaskTerm(participantTaskDao, arguments.get(0), arguments.get(1));
    }

    @Override
    public String getTermName() {
        return "task";
    }

    @Override
    public Map<String, SearchValueTypeDefinition> getFacets(UUID studyEnvId) {
        Map<String, SearchValueTypeDefinition> fields = new HashMap<>();
        List<Survey> surveys = surveyService.findByStudyEnvironmentIdWithContent(studyEnvId);
        for (Survey survey : surveys) {
            TaskTerm.FIELDS.forEach((term, type) -> fields.put("task." + survey.getStableId() + "." + term, type));
        }

        return fields;
    }
}
