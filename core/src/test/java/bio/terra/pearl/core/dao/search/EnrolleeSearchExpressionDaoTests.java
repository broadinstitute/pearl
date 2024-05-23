package bio.terra.pearl.core.dao.search;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantTaskFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.search.EnrolleeSearchExpressionResult;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpression;
import bio.terra.pearl.core.service.search.EnrolleeSearchExpressionParser;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnrolleeSearchExpressionDaoTests extends BaseSpringBootTest {

    @Autowired
    EnrolleeSearchExpressionDao enrolleeSearchExpressionDao;

    @Autowired
    EnrolleeFactory enrolleeFactory;

    @Autowired
    StudyEnvironmentFactory studyEnvironmentFactory;

    @Autowired
    EnrolleeSearchExpressionParser enrolleeSearchExpressionParser;

    @Autowired
    SurveyFactory surveyFactory;

    @Autowired
    SurveyResponseFactory surveyResponseFactory;

    @Autowired
    ParticipantTaskFactory participantTaskFactory;


    @Test
    @Transactional
    public void testExecuteAnswerSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        StudyEnvironment otherEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));


        Survey survey = surveyFactory.buildPersisted(getTestName(info));

        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule(
                "{profile.givenName} = 'Jonas' and {profile.familyName} = 'Salk' and {answer.%s.test_question} = 'answer'".formatted(survey.getStableId())
        );
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        Assertions.assertEquals(0, enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId()).size());

        // correct enrollee
        Enrollee salk = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());
        // no answer but correct name
        enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());
        // wrong last name but correct answer
        Enrollee smith = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Smith").build());
        // wrong study env
        Enrollee wrongEnv = enrolleeFactory.buildPersisted(
                getTestName(info),
                otherEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());


        surveyResponseFactory.buildWithAnswers(
                salk,
                survey,
                Map.of("test_question", "answer")
        );

        surveyResponseFactory.buildWithAnswers(
                smith,
                survey,
                Map.of("test_question", "answer")
        );

        surveyResponseFactory.buildWithAnswers(
                wrongEnv,
                survey,
                Map.of("test_question", "answer")
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(1, results.size());
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(salk.getId())));
    }

    @Test
    @Transactional
    public void testAnswerAttachedToResult(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        Survey diffSurvey = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);
        surveyFactory.attachToEnv(diffSurvey, studyEnv.getId(), true);

        String response = RandomStringUtils.randomAlphanumeric(20);

        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule(
                "{answer.%s.example_question} = '%s'".formatted(survey.getStableId(), response)
        );

        Enrollee enrollee = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv);

        surveyResponseFactory.buildWithAnswers(
                enrollee,
                survey,
                Map.of(
                        "example_question", response,
                        "another_question", "something else")
        );

        surveyResponseFactory.buildWithAnswers(
                enrollee,
                diffSurvey,
                Map.of(
                        "example_question", "NOT THE CORRECT ONE",
                        "another_question", "something else")
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(1, results.size());

        EnrolleeSearchExpressionResult result = results.get(0);

        Assertions.assertEquals(enrollee.getId(), result.getEnrollee().getId());

        Assertions.assertEquals(response, result.getAnswers().get(0).getStringValue());
        Assertions.assertEquals(survey.getStableId(), result.getAnswers().get(0).getSurveyStableId());
    }

    @Test
    @Transactional
    public void testMultipleAnswerSearch(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        Survey survey1 = surveyFactory.buildPersisted(getTestName(info));
        Survey survey2 = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey1, studyEnv.getId(), true);
        surveyFactory.attachToEnv(survey2, studyEnv.getId(), true);


        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule(
                "{answer.%s.example_question} = 'asdf' and {answer.%s.other_question} = 'hjkl'".formatted(
                        survey1.getStableId(),
                        survey2.getStableId())
        );

        Enrollee enrollee = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv);

        Enrollee onlyMatchesOne = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv);

        surveyResponseFactory.buildWithAnswers(
                enrollee,
                survey1,
                Map.of("example_question", "asdf")
        );

        surveyResponseFactory.buildWithAnswers(
                enrollee,
                survey2,
                Map.of("other_question", "hjkl")
        );

        surveyResponseFactory.buildWithAnswers(
                onlyMatchesOne,
                survey1,
                Map.of("example_question", "asdf")
        );

        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(1, results.size());
        EnrolleeSearchExpressionResult result = results.get(0);
        Assertions.assertEquals(enrollee.getId(), result.getEnrollee().getId());

        Assertions.assertEquals(2, result.getAnswers().size());
        Assertions.assertTrue(result.getAnswers().stream().anyMatch(
                a -> a.getSurveyStableId().equals(survey1.getStableId()) && a.getQuestionStableId().equals("example_question")));
        Assertions.assertTrue(result.getAnswers().stream().anyMatch(
                a -> a.getSurveyStableId().equals(survey2.getStableId()) && a.getQuestionStableId().equals("other_question")));
    }

    @Test
    @Transactional
    public void testAgeFacet(TestInfo info) {
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(getTestName(info));
        EnrolleeSearchExpression exp = enrolleeSearchExpressionParser.parseRule(
                "{age} > 25 and {age} <= 30"
        );

        enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(32)).build());
        Enrollee e1 = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(27)).build());
        Enrollee e2 = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(26)).build());
        Enrollee e3 = enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(30)).build());
        enrolleeFactory.buildPersisted(
                getTestName(info),
                studyEnv,
                Profile.builder().birthDate(LocalDate.now().minusYears(25)).build());


        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(exp, studyEnv.getId());

        Assertions.assertEquals(3, results.size());

        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(e1.getId())));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(e2.getId())));
        Assertions.assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(e3.getId())));
    }

    @Test
    @Transactional
    public void testAnswersWithParensEvaluate(TestInfo info) {
        StudyEnvironment studyEnvironment = studyEnvironmentFactory.buildPersisted(getTestName(info));

        Survey survey = surveyFactory.buildPersisted(getTestName(info));
        surveyFactory.attachToEnv(survey, studyEnvironment.getId(), true);

        Enrollee enrolleeMatches1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeMatches1, survey, Map.of(
                "diagnosis", "diag1",
                "country", "us"

        ));

        Enrollee enrolleeMatches2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeMatches2, survey, Map.of(
                "diagnosis", "diag2",
                "country", "us"
        ));

        Enrollee enrolleeDoesNotMatch1 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeDoesNotMatch1, survey, Map.of(
                "diagnosis", "diag3",
                "country", "us"
        ));

        Enrollee enrolleeDoesNotMatch2 = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        surveyResponseFactory.buildWithAnswers(enrolleeDoesNotMatch2, survey, Map.of(
                "diagnosis", "diag2",
                "country", "gb"
        ));

        // enrollee with no response
        enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);


        String rule = "{answer.%s.country} = 'us' and ({answer.%s.diagnosis} = 'diag1' or {answer.%s.diagnosis} = 'diag2')".formatted(survey.getStableId(), survey.getStableId(), survey.getStableId());
        EnrolleeSearchExpression searchExp = enrolleeSearchExpressionParser.parseRule(rule);


        List<EnrolleeSearchExpressionResult> results = enrolleeSearchExpressionDao.executeSearch(searchExp, studyEnvironment.getId());

        Assertions.assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeMatches1.getId())));
        assertTrue(results.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeMatches2.getId())));
    }


    @Test
    @Transactional
    public void testTaskFacets(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        EnrolleeSearchExpression assignedExp = enrolleeSearchExpressionParser.parseRule(
                "{task.demographic_survey.assigned} = true"
        );

        EnrolleeSearchExpression inProgressExp = enrolleeSearchExpressionParser.parseRule(
                "{task.demographic_survey.status} = 'IN_PROGRESS'"
        );

        // enrollee not assigned
        EnrolleeFactory.EnrolleeBundle eBundleNotAssigned = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        Enrollee enrolleeNotAssigned = eBundleNotAssigned.enrollee();

        // enrollee assigned not started
        EnrolleeFactory.EnrolleeBundle eBundleNotStarted = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(eBundleNotStarted, "demographic_survey", TaskStatus.NEW, TaskType.SURVEY);
        Enrollee enrolleeNotStarted = eBundleNotStarted.enrollee();

        // enrollee assigned in progress
        EnrolleeFactory.EnrolleeBundle eBundleInProgress = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(eBundleInProgress, "demographic_survey", TaskStatus.IN_PROGRESS, TaskType.SURVEY);
        Enrollee enrolleeInProgress = eBundleInProgress.enrollee();

        // enrollee assigned in progress but different task
        EnrolleeFactory.EnrolleeBundle eBundleInProgressWrongTask = enrolleeFactory.buildWithPortalUser(getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
        participantTaskFactory.buildPersisted(eBundleInProgressWrongTask, "something_else", TaskStatus.IN_PROGRESS, TaskType.SURVEY);

        List<EnrolleeSearchExpressionResult> resultsAssigned = enrolleeSearchExpressionDao.executeSearch(assignedExp, studyEnvBundle.getStudyEnv().getId());
        List<EnrolleeSearchExpressionResult> resultsInProgress = enrolleeSearchExpressionDao.executeSearch(inProgressExp, studyEnvBundle.getStudyEnv().getId());

        Assertions.assertEquals(2, resultsAssigned.size());
        Assertions.assertEquals(1, resultsInProgress.size());

        assertTrue(resultsAssigned.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeNotStarted.getId())));
        assertTrue(resultsAssigned.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeInProgress.getId())));

        assertTrue(resultsInProgress.stream().anyMatch(r -> r.getEnrollee().getId().equals(enrolleeInProgress.getId())));

        // attaches the task to the enrollee search result
        assertTrue(resultsInProgress.stream().allMatch(r -> r.getTasks().size() == 1 && r.getTasks().get(0).getTargetStableId().equals("demographic_survey")));
        assertTrue(resultsAssigned.stream().allMatch(r -> r.getTasks().size() == 1 && r.getTasks().get(0).getTargetStableId().equals("demographic_survey")));
    }

    @Test
    @Transactional
    public void testEnrolleeFacets(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        UUID studyEnvId = studyEnvBundle.getStudyEnv().getId();
        EnrolleeSearchExpression consentedExp = enrolleeSearchExpressionParser.parseRule(
                "{enrollee.consented} = true"
        );

        EnrolleeSearchExpression subjectExp = enrolleeSearchExpressionParser.parseRule(
                "{enrollee.subject} = true"
        );

        EnrolleeSearchExpression shortcodeExp = enrolleeSearchExpressionParser.parseRule(
                "{enrollee.shortcode} = 'EXAMPLE'"
        );

        Enrollee notConsented = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).consented(false).subject(true).studyEnvironmentId(studyEnvId));

        Enrollee notSubject = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).consented(false).subject(false).studyEnvironmentId(studyEnvId));

        Enrollee specialShortcode = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).consented(false).subject(false).shortcode("EXAMPLE").studyEnvironmentId(studyEnvId));

        Enrollee consented = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).consented(true).subject(true).studyEnvironmentId(studyEnvId));

        List<EnrolleeSearchExpressionResult> resultsConsented = enrolleeSearchExpressionDao.executeSearch(consentedExp, studyEnvBundle.getStudyEnv().getId());
        List<EnrolleeSearchExpressionResult> resultsSubject = enrolleeSearchExpressionDao.executeSearch(subjectExp, studyEnvBundle.getStudyEnv().getId());
        List<EnrolleeSearchExpressionResult> resultsShortcode = enrolleeSearchExpressionDao.executeSearch(shortcodeExp, studyEnvBundle.getStudyEnv().getId());

        Assertions.assertEquals(1, resultsConsented.size());
        Assertions.assertEquals(2, resultsSubject.size());
        Assertions.assertEquals(1, resultsShortcode.size());

        assertTrue(resultsConsented.stream().anyMatch(r -> r.getEnrollee().getId().equals(consented.getId())));

        assertTrue(resultsSubject.stream().anyMatch(r -> r.getEnrollee().getId().equals(notConsented.getId())));
        assertTrue(resultsSubject.stream().anyMatch(r -> r.getEnrollee().getId().equals(consented.getId())));

        assertTrue(resultsShortcode.stream().anyMatch(r -> r.getEnrollee().getId().equals(specialShortcode.getId())));
    }

    @Test
    @Transactional
    public void testContains(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        UUID studyEnvId = studyEnvBundle.getStudyEnv().getId();

        EnrolleeSearchExpression shortcodeExp = enrolleeSearchExpressionParser.parseRule(
                "{enrollee.shortcode} contains 'JSA'"
        );

        Enrollee startsWith = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).shortcode("JSALK").studyEnvironmentId(studyEnvId));

        Enrollee within = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).shortcode("ASDJSAF").studyEnvironmentId(studyEnvId));

        Enrollee endsWith = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).shortcode("ASDFJSA").studyEnvironmentId(studyEnvId));

        Enrollee doesNotContain = enrolleeFactory.buildPersisted(
                enrolleeFactory.builderWithDependencies(getTestName(info)).shortcode("PSALK").studyEnvironmentId(studyEnvId));

        List<EnrolleeSearchExpressionResult> resultsShortcode = enrolleeSearchExpressionDao.executeSearch(shortcodeExp, studyEnvBundle.getStudyEnv().getId());

        Assertions.assertEquals(3, resultsShortcode.size());
        assertTrue(resultsShortcode.stream().anyMatch(r -> r.getEnrollee().getId().equals(startsWith.getId())));
        assertTrue(resultsShortcode.stream().anyMatch(r -> r.getEnrollee().getId().equals(within.getId())));
        assertTrue(resultsShortcode.stream().anyMatch(r -> r.getEnrollee().getId().equals(endsWith.getId())));
    }

    @Test
    @Transactional
    public void testProfileName(TestInfo info) {
        StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
        PortalEnvironment portalEnv = studyEnvBundle.getPortalEnv();
        StudyEnvironment studyEnv = studyEnvBundle.getStudyEnv();

        EnrolleeSearchExpression nameExp = enrolleeSearchExpressionParser.parseRule(
                "{profile.name} = 'Jonas Salk'"
        );


        EnrolleeFactory.EnrolleeBundle jsalkBundle = enrolleeFactory.buildWithPortalUser(
                getTestName(info),
                portalEnv,
                studyEnv,
                Profile.builder().givenName("Jonas").familyName("Salk").build());

        EnrolleeFactory.EnrolleeBundle psalkBundle = enrolleeFactory.buildWithPortalUser(
                getTestName(info),
                portalEnv,
                studyEnv,
                Profile.builder().givenName("Peter").familyName("Salk").build());

        EnrolleeFactory.EnrolleeBundle reversedBundle = enrolleeFactory.buildWithPortalUser(
                getTestName(info),
                portalEnv,
                studyEnv,
                Profile.builder().givenName("Salk").familyName("Jonas").build());


        List<EnrolleeSearchExpressionResult> resultsName = enrolleeSearchExpressionDao.executeSearch(nameExp, studyEnv.getId());

        Assertions.assertEquals(1, resultsName.size());
        assertTrue(resultsName.stream().anyMatch(r -> r.getEnrollee().getId().equals(jsalkBundle.enrollee().getId())));

    }
}
