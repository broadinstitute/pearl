package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.item.AnswerItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * See https://broad-juniper.zendesk.com/hc/en-us/articles/18259824756123-Participant-List-Export-details
 * for information on the export format of survey questions
 */
@Slf4j
public class SurveyFormatter extends ModuleFormatter<SurveyResponseWithTaskDto, ItemFormatter<SurveyResponseWithTaskDto>> {
    public static String OTHER_DESCRIPTION_KEY_SUFFIX = "_description";
    public static String OTHER_DESCRIPTION_HEADER = "other description";
    public static String SPLIT_OPTION_SELECTED_VALUE = "1";
    public static String SPLIT_OPTION_UNSELECTED_VALUE = "0";
    private ObjectMapper objectMapper;
    private final List<UUID> surveyIds; // the list of surveyIds that are included in this module -- used for filtering responses

    public SurveyFormatter(ExportOptions exportOptions,
                           String stableId,
                           List<Survey> surveys,
                           List<SurveyQuestionDefinition> questionDefs,
                           List<EnrolleeExportData> data,
                           ObjectMapper objectMapper) {
        super(exportOptions,
                stableId,
                // get the most recent survey by sorting in descending version order
                surveys.stream().sorted(Comparator.comparingInt(Survey::getVersion).reversed()).findFirst().get().getName());
        this.objectMapper = objectMapper;
        this.surveyIds = surveys.stream().map(Survey::getId).toList();
        generateAnswerItemFormatters(exportOptions, questionDefs, data);
        filterItemFormatters(exportOptions);
    }

    @Override
    protected List<ItemFormatter<SurveyResponseWithTaskDto>> generateItemFormatters(ExportOptions options) {
        // Note that we generate and add answer formatters to this list later in the constructor
        List<ItemFormatter<SurveyResponseWithTaskDto>> formatters = new ArrayList<>();
        formatters.add(new PropertyItemFormatter<>("lastUpdatedAt", SurveyResponseWithTaskDto.class, options.getZoneId()));
        formatters.add(new PropertyItemFormatter<>("createdAt", SurveyResponseWithTaskDto.class, options.getZoneId()));
        formatters.add(new PropertyItemFormatter<>("responseMetadata", SurveyResponseWithTaskDto.class, options.getZoneId()));

        PropertyItemFormatter<SurveyResponseWithTaskDto> taskCompletedFormatter = new PropertyItemFormatter<>("task.completedAt", SurveyResponseWithTaskDto.class, "completedAt", options.getZoneId());
        taskCompletedFormatter.setImportable(false); // task will be null at time of initial import, but time shifting will occur after
        formatters.add(taskCompletedFormatter);

        formatters.add(new PropertyItemFormatter<>("complete", SurveyResponseWithTaskDto.class, options.getZoneId()));
        return formatters;
    }

    private void generateAnswerItemFormatters(
            ExportOptions exportOptions,
            List<SurveyQuestionDefinition> questionDefs,
            List<EnrolleeExportData> data) {
        // group all the questions that share a stableId (i.e. different versions of the same question), and then sort them by
        // the export order of the most recent version
        Collection<List<SurveyQuestionDefinition>> questionDefsByStableId = questionDefs.stream().collect(groupingBy(
                SurveyQuestionDefinition::getQuestionStableId
        )).values().stream().sorted(Comparator.comparingInt(a -> a.get(0).getExportOrder())).toList();

        for (List<SurveyQuestionDefinition> questionVersions : questionDefsByStableId) {
            SurveyQuestionDefinition mostRecent = questionVersions.get(0);

            if (List.of("signaturepad", "html").contains(mostRecent.getQuestionType())) {
                continue;
            }

            if (StringUtils.isNotEmpty(mostRecent.getParentStableId())) {
                // we'll handle these when we get to the parent question
                continue;
            }

            itemFormatters.add(new AnswerItemFormatter(exportOptions, moduleName, questionVersions, objectMapper));

            itemFormatters.addAll(buildChildrenItemFormatters(exportOptions, questionDefsByStableId, data, mostRecent));

        }
    }

    private Collection<List<SurveyQuestionDefinition>> getChildrenOf(Collection<List<SurveyQuestionDefinition>> questionDefs, SurveyQuestionDefinition parent) {
        return questionDefs.stream().filter(questionDef -> parent.getQuestionStableId().equals(questionDef.get(0).getParentStableId())).toList();
    }

    private List<ItemFormatter<SurveyResponseWithTaskDto>> buildChildrenItemFormatters(
            ExportOptions exportOptions,
            Collection<List<SurveyQuestionDefinition>> questionDefs,
            List<EnrolleeExportData> data,
            SurveyQuestionDefinition parent) {

        Collection<List<SurveyQuestionDefinition>> children = getChildrenOf(questionDefs, parent);
        if (children.isEmpty()) {
            return Collections.emptyList();
        }

        if (parent.isRepeatable()) {
            return buildRepeatableChildrenItemFormatters(exportOptions, data, parent, children);
        }

        List<ItemFormatter<SurveyResponseWithTaskDto>> childrenItemFormatters = new ArrayList<>();
        for (List<SurveyQuestionDefinition> childVersions : children) {
            childrenItemFormatters.add(new AnswerItemFormatter(exportOptions, moduleName, childVersions, objectMapper));
        }
        return childrenItemFormatters;
    }

    private List<ItemFormatter<SurveyResponseWithTaskDto>> buildRepeatableChildrenItemFormatters(
            ExportOptions exportOptions,
            List<EnrolleeExportData> data,
            SurveyQuestionDefinition parent,
            Collection<List<SurveyQuestionDefinition>> children) {
        int maxParentResponseLength = data
                .stream()
                .flatMap(enrolleeData -> enrolleeData.getAnswers().stream())
                .filter(answer -> parent.getQuestionStableId().equals(answer.getQuestionStableId()))
                .mapToInt(answer -> {

                    String value = StringUtils.isEmpty(answer.getStringValue()) ? answer.getObjectValue() : answer.getStringValue();
                    if (StringUtils.isEmpty(value)) {
                        return 0;
                    }

                    try {
                        return objectMapper.readTree(value).size();
                    } catch (JsonProcessingException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(1);

        if (maxParentResponseLength < 1) {
            // always return one so that we can still introspect
            // on the data even if no data exists
            maxParentResponseLength = 1;
        }

        List<ItemFormatter<SurveyResponseWithTaskDto>> childrenItemFormatters = new ArrayList<>();
        for (int repeat = 0; repeat < maxParentResponseLength; repeat++) {
            for (List<SurveyQuestionDefinition> childVersions : children) {
                childrenItemFormatters.add(new AnswerItemFormatter(exportOptions, moduleName, childVersions, objectMapper, repeat));
            }
        }

        return childrenItemFormatters;
    }


    @Override
    public String getColumnKey(ItemFormatter itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        return getColumnKeyChoiceStableId(itemFormatter, isOtherDescription, choice == null ? null : choice.stableId(), moduleRepeatNum);
    }

    public String getColumnKeyChoiceStableId(ItemFormatter itemFormatter, boolean isOtherDescription, String choiceStableId, int moduleRepeatNum) {
        String columnKey = super.getColumnKey(itemFormatter, isOtherDescription, null, moduleRepeatNum);
        if (isOtherDescription) {
            columnKey += OTHER_DESCRIPTION_KEY_SUFFIX;
        } else if (choiceStableId != null && ((AnswerItemFormatter) itemFormatter).isSplitOptionsIntoColumns()) {
            columnKey += ExportFormatUtils.COLUMN_NAME_DELIMITER + choiceStableId;
        }
        return columnKey;
    }

    /**
     * this method largely mirrors "getColumnKey", but it strips out the prefixes to make the headers more readable
     */
    @Override
    public String getColumnHeader(ItemFormatter itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        String columnHeader = super.getColumnHeader(itemFormatter, isOtherDescription, choice, moduleRepeatNum);
        if (itemFormatter instanceof AnswerItemFormatter) {
            AnswerItemFormatter answerItemFormatter = (AnswerItemFormatter) itemFormatter;
            // for now, strip the prefixes to aid in readability.  Once we have multi-source surveys, we can revisit this.
            String cleanStableId = stripStudyAndSurveyPrefixes(answerItemFormatter.getQuestionStableId());
            if (Objects.nonNull(answerItemFormatter.getParentStableId())) {
                cleanStableId = stripStudyAndSurveyPrefixes(answerItemFormatter.getParentStableId()) + ExportFormatUtils.COLUMN_NAME_DELIMITER + cleanStableId;
            }
            if (Objects.nonNull(answerItemFormatter.getRepeatIndex())) {
                cleanStableId += ExportFormatUtils.formatIndex(answerItemFormatter.getRepeatIndex());
            }
            String moduleRepeatString = moduleRepeatNum > 1 ? ExportFormatUtils.formatIndex(moduleRepeatNum) : "";
            columnHeader = moduleName + moduleRepeatString + ExportFormatUtils.COLUMN_NAME_DELIMITER + cleanStableId;
            if (isOtherDescription) {
                columnHeader += OTHER_DESCRIPTION_KEY_SUFFIX;
            } else if (answerItemFormatter.isSplitOptionsIntoColumns() && choice != null) {
                // the null check above is because even when we are splitting options into columns, we might need to
                // get a column header for the question as a whole (e.g. for the data dictionary), in which case
                // we would call this method with a null choice
                columnHeader += ExportFormatUtils.COLUMN_NAME_DELIMITER + choice.stableId();
            }
        }
            return columnHeader;
    }

    /**
     * returns either the question or the choice as friendly-ish text
     */
    @Override
    public String getColumnSubHeader(ItemFormatter itemFormatter, boolean isOtherDescription, QuestionChoice choice, int moduleRepeatNum) {
        if (itemFormatter instanceof PropertyItemFormatter) {
            return ExportFormatUtils.camelToWordCase(((PropertyItemFormatter) itemFormatter).getPropertyName());
        }
        AnswerItemFormatter answerItemFormatter = (AnswerItemFormatter) itemFormatter;
        if (answerItemFormatter.isSplitOptionsIntoColumns() && choice != null) {
            return choice.text();
        }
        if (isOtherDescription) {
            return OTHER_DESCRIPTION_HEADER;
        }
        return ExportFormatUtils.camelToWordCase(stripStudyAndSurveyPrefixes(answerItemFormatter.getQuestionStableId()));
    }


    private static final Pattern OLD_STABLE_ID_PATTERN = Pattern.compile("^[a-z]{2}_[a-z]{2}_[a-zA-Z]+_");
    /**
     * strip out study and survey prefixes.  so e.g. "oh_oh_famHx_question1" becomes "question1"
     */
    public static String stripStudyAndSurveyPrefixes(String stableId) {

        // warning: hacky!
        // if the stable id has the old format (e.g. oh_oh_basicInfo_question)
        // then we need to strip out the first three parts
        if (OLD_STABLE_ID_PATTERN.matcher(stableId).find()) {
            return OLD_STABLE_ID_PATTERN.matcher(stableId).replaceFirst("");
        }

        return stableId;
    }

    /**
     * strip out survey prefixes.  so e.g. "oh_oh_famHx.oh_oh_famHx_question1" becomes "oh_oh_famHx.question1"
     */
    public static String stripSurveyPrefix(String columnKey) {
        String[] parts = columnKey.split("\\.");
        if (parts.length == 2) {
            return "%s.%s".formatted(parts[0], stripStudyAndSurveyPrefixes(parts[1]));
        }
        return columnKey;
    }

    @Override
    public Map<String, String> toStringMap(EnrolleeExportData exportData) {
        Map<String, String> valueMap = new HashMap<>();
        List<SurveyResponseWithTaskDto> responses = exportData.getResponses().stream()
                .filter(response -> surveyIds.contains(response.getSurveyId()))
                .toList();
        for (int i = 0; i < responses.size(); i++) {
            addResponseToMap(responses.get(i), exportData, valueMap, i + 1);
        }
        maxNumRepeats = Math.max(maxNumRepeats, responses.size());
        return valueMap;
    }

    // note that responseNum is 1-indexed, not zero-index since it goes to moduleRepeatNum
    protected void addResponseToMap(SurveyResponse surveyResponse, EnrolleeExportData exportData, Map<String, String> valueMap, int responseNum) {
        List<Answer> answers = exportData.getAnswers().stream().filter(ans ->
                Objects.equals(ans.getSurveyResponseId(), surveyResponse.getId())
        ).toList();

        // map the answers by question stable id for easier access
        Map<String, List<Answer>> answerMap = answers.stream().collect(groupingBy(Answer::getQuestionStableId));

        for (ItemFormatter itemFormatter : getItemFormatters()) {
            if (itemFormatter instanceof PropertyItemFormatter) {
                // it's a property of the SurveyResponse
                valueMap.put(getColumnKey(itemFormatter, false, null, responseNum),
                        ((PropertyItemFormatter) itemFormatter).getExportString(surveyResponse));
            } else {
                // it's an answer value
                addAnswersToMap((AnswerItemFormatter) itemFormatter, answerMap, valueMap, responseNum);
            }
        }
    }


    public void addAnswersToMap(AnswerItemFormatter itemFormatter,
                                Map<String, List<Answer>> answerMap, Map<String, String> valueMap, int responseNum) {
        String valueStableId = itemFormatter.getQuestionStableId();

        // if the question is a child of another question, then we need to get the parent question's value
        // so that the child answer can be extracted from it
        if (itemFormatter.isChildQuestion()) {
            valueStableId = itemFormatter.getParentStableId();
        }

        List<Answer> matchedAnswers = answerMap.get(valueStableId);
        if (matchedAnswers == null) {
            return;
        }
        // for now, we only support one answer per question, so just return the first
        Answer matchedAnswer = matchedAnswers.get(0);
        // use the ItemExport Info matching the answer version so choices get translated correctly
        AnswerItemFormatter matchedItemFormatter = itemFormatter.getVersionMap().get(matchedAnswer.getSurveyVersion());
        if (matchedItemFormatter == null) {
            // if we can't find a match (likely because we're in a demo environment and the answer refers to a version that no longer exists)
            // just use the current version
            matchedItemFormatter = itemFormatter;
        }
        addAnswerToMap(matchedItemFormatter, matchedAnswer, valueMap, objectMapper, responseNum);
    }

    protected void addAnswerToMap(AnswerItemFormatter itemFormatter,
                                  Answer answer, Map<String, String> valueMap, ObjectMapper objectMapper, int responseNum) {
        if (itemFormatter.isSplitOptionsIntoColumns()) {
            addSplitOptionSelectionsToMap(itemFormatter, answer, valueMap, objectMapper, responseNum);
        } else {
            valueMap.put(
                    getColumnKey(itemFormatter, false, null, responseNum),
                    valueAsString(itemFormatter, answer, itemFormatter.getChoices(), itemFormatter.isStableIdsForOptions(), objectMapper)
            );
        }
        if (itemFormatter.isHasOtherDescription() && answer.getOtherDescription() != null) {
            valueMap.put(
                    getColumnKey(itemFormatter, true, null, responseNum),
                    answer.getOtherDescription()
            );
        }
    }

    protected static String valueAsString(AnswerItemFormatter itemFormatter, Answer answer, List<QuestionChoice> choices, boolean stableIdForOptions, ObjectMapper objectMapper) {
        if (itemFormatter.isChildQuestion()) {
            // if the question is a child of a parent question, then the answer value is
            // the parent's json object value, so we need to extract the value of this
            // child's stable id from it
            answer = extractChildAnswer(itemFormatter, answer, choices, stableIdForOptions, objectMapper);
        }

        if (answer.getStringValue() != null) {
            return formatStringValue(answer.getStringValue(), choices, stableIdForOptions, answer);
        } else if (answer.getBooleanValue() != null) {
            return answer.getBooleanValue() ? "true" : "false";
        } else if (answer.getNumberValue() != null) {
            return answer.getNumberValue().toString();
        } else if (answer.getObjectValue() != null) {
            return formatObjectValue(answer, choices, stableIdForOptions, objectMapper);
        }
        return "";
    }

    protected static Answer extractChildAnswer(AnswerItemFormatter itemFormatter, Answer answer, List<QuestionChoice> choices, boolean stableIdForOptions, ObjectMapper objectMapper) {
        Integer repeatIndex = itemFormatter.getRepeatIndex();
        Answer childAnswer = new Answer();
        BeanUtils.copyProperties(answer, childAnswer, "objectValue", "stringValue");

        try {
            if (answer.getParsedObjectValue() == null) {
                answer.setParsedObjectValue(objectMapper.readTree(answer.getObjectValue()));
            }
            JsonNode answerNode = answer.getParsedObjectValue();
            if (Objects.nonNull(repeatIndex)) {
                if (!answerNode.has(repeatIndex)) {
                    return childAnswer;
                }
                answerNode = answerNode.get(repeatIndex);
            }

            if (!answerNode.has(itemFormatter.getQuestionStableId())) {
                return childAnswer;
            }
            answerNode = answerNode.get(itemFormatter.getQuestionStableId());

            if (answerNode == null) {
                return childAnswer;
            }
            if (answerNode.isArray()) {
                childAnswer.setObjectValue(answerNode.toString());

            } else if (answerNode.isBoolean()) {
                childAnswer.setBooleanValue(answerNode.asBoolean());

            } else if (answerNode.isNumber()) {
                childAnswer.setNumberValue(answerNode.asDouble());
            } else if (answerNode.isTextual()) {
                childAnswer.setStringValue(answerNode.asText());
            }
        } catch (Exception e) {
            log.warn("Failed to parse parent answer for child question - enrollee: {}, question: {}, answer: {}",
                    answer.getEnrolleeId(), answer.getQuestionStableId(), answer.getId());
        }
        return childAnswer;
    }

    /**
     * adds an entry to the valueMap for each selected option of a 'splitOptionsIntoColumns' question
     */
    protected void addSplitOptionSelectionsToMap(ItemFormatter itemFormatter,
                                                 Answer answer, Map<String, String> valueMap, ObjectMapper objectMapper, int responseNum) {
        if (answer.getStringValue() != null) {
            // this was a single-select question, so we only need to add the selected option
            valueMap.put(
                    getColumnKeyChoiceStableId(itemFormatter, false, answer.getStringValue(), responseNum),
                    SPLIT_OPTION_SELECTED_VALUE
            );
        } else if (answer.getObjectValue() != null) {
            // this was a multi-select question, so we need to add all selected options
            try {
                List<String> answerValues = objectMapper.readValue(answer.getObjectValue(), new TypeReference<List<String>>() {
                });
                for (String answerValue : answerValues) {
                    valueMap.put(
                            getColumnKeyChoiceStableId(itemFormatter, false, answerValue, responseNum),
                            SPLIT_OPTION_SELECTED_VALUE
                    );
                }
            } catch (JsonProcessingException e) {
                // don't stop the entire export for one bad value, see JN-650 for aggregating these to user messages
                log.error("Error parsing answer object value enrollee: {}, question: {}, answer: {}",
                        answer.getEnrolleeId(), answer.getQuestionStableId(), answer.getId());
            }
        }
    }

    protected static String formatStringValue(String value, List<QuestionChoice> choices, boolean stableIdForOptions, Answer answer) {
        if (stableIdForOptions || choices == null || choices.isEmpty()) {
            return value;
        }
        QuestionChoice matchedChoice = choices.stream().filter(choice ->
                Objects.equals(choice.stableId(), value)).findFirst().orElse(null);
        if (matchedChoice == null) {
            log.warn("Unmatched answer option -  enrollee: {}, question: {}, answer: {}",
                    answer.getEnrolleeId(), answer.getQuestionStableId(), answer.getId());
            return value;
        }
        return matchedChoice.text();
    }

    protected static String formatObjectValue(Answer answer, List<QuestionChoice> choices, boolean stableIdForOptions, ObjectMapper objectMapper) {
        try {
            // for now, the only object values we support explicitly parsing are arrays of strings
            String[] answerArray = objectMapper.readValue(answer.getObjectValue(), String[].class);
            if (stableIdForOptions) {
                return StringUtils.join(answerArray, ", ");
            }
            return Arrays.stream(answerArray).map(ansValue -> formatStringValue(ansValue, choices, stableIdForOptions, answer))
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            // if we don't know what to do with it, just return the raw value
            return answer.getObjectValue();
        }
    }

    public List<SurveyResponseWithTaskDto> listFromStringMap(UUID studyEnvironmentId, Map<String, String> enrolleeMap) {
        List<SurveyResponseWithTaskDto> responses = new ArrayList<>();
        int moduleRepeatNum = 1;
        SurveyResponseWithTaskDto response = fromStringMap(studyEnvironmentId, enrolleeMap, moduleRepeatNum);
        while (response != null) {
            responses.add(response);
            moduleRepeatNum++;
            response = fromStringMap(studyEnvironmentId, enrolleeMap, moduleRepeatNum);
        }
        return responses;
    }

    @Override
    public SurveyResponseWithTaskDto fromStringMap(UUID studyEnvironmentId, Map<String, String> enrolleeMap, int moduleRepeatNum) {
        SurveyResponseWithTaskDto response = new SurveyResponseWithTaskDto();
        boolean specifiedComplete = false;
        for (ItemFormatter<SurveyResponseWithTaskDto> itemFormatter : itemFormatters) {
            String columnName = getColumnKey(itemFormatter, false, null, moduleRepeatNum);
            if (!enrolleeMap.containsKey(columnName)) {
                //try stripping surveyName
                columnName = stripSurveyPrefix(columnName);
            }
            String stringVal = enrolleeMap.get(columnName);

            // track whether the complete field was explicitly set
            if (itemFormatter.getBaseColumnKey().equals("complete") && stringVal != null) {
                specifiedComplete = true;
            }

            if (!itemFormatter.isImportable()) {
                continue;
            }

            if (stringVal != null && !stringVal.isEmpty()) {

                itemFormatter.importValueToBean(response, stringVal);
            }
        }
        // default to complete if not specified otherwise and there are answers
        if (!specifiedComplete && !response.getAnswers().isEmpty()) {
            response.setComplete(true);
        }

        return (response.getAnswers().isEmpty() ? null : response);
    }
}
