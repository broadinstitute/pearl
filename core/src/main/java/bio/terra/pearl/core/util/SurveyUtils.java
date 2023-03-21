package bio.terra.pearl.core.util;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyUtils {

    public static List<JsonNode> getAllQuestions(JsonNode containerElement) {
        List<JsonNode> elements = new ArrayList<>();

        if(containerElement.has("elements")) {
            for(JsonNode element : containerElement.get("elements")) {
                elements.addAll(getAllQuestions(element));
            }
        } else elements.add(containerElement);

        return elements;
    }

    public static SurveyQuestionDefinition unmarshalSurveyQuestion(Survey survey, JsonNode question, Map<String, JsonNode> questionTemplates) {
        SurveyQuestionDefinition definition = new SurveyQuestionDefinition();

        definition.setSurveyId(survey.getId());
        definition.setSurveyStableId(survey.getStableId());
        definition.setSurveyVersion(survey.getVersion());
        definition.setQuestionStableId(question.get("name").asText());

        //The following fields may either be specified in the question itself,
        //or as part of a question template. Resolve the remaining fields against
        //the template (if applicable), so we have the full question definition.
        JsonNode templatedQuestion = question.has("questionTemplateName") ?
                questionTemplates.get(question.get("questionTemplateName").asText()) :
                question;

        definition.setQuestionType(templatedQuestion.get("type").asText());

        if(templatedQuestion.has("isRequired")){
            definition.setRequired(templatedQuestion.get("isRequired").asBoolean());
        } else definition.setRequired(false);

        if(templatedQuestion.has("choices")){
            definition.setChoices(unmarshalSurveyQuestionChoices(templatedQuestion));
        }

        return definition;
    }

    public static String unmarshalSurveyQuestionChoices(JsonNode question) {
        Map<String, String> choices = new HashMap<>();
        for(JsonNode choice : question.get("choices")){
            choices.put(choice.get("value").asText(), choice.get("text").asText());
        }
        ObjectMapper mapper = new ObjectMapper();

        String result;
        try {
            result = mapper.writeValueAsString(choices);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Question contains malformed choices");
        }

        return result;
    }

}
