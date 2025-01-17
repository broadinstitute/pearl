package bio.terra.pearl.populate;

import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import bio.terra.pearl.populate.service.SurveyPopulator;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

public class SurveyPopulatorTests extends BaseSpringBootTest {
    @Autowired
    SurveyPopulator surveyPopulator;
    @Autowired
    private Jdbi jdbi;
    @Autowired
    SurveyService surveyService;
    @Autowired
    SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;
    @Autowired
    SurveyFactory surveyFactory;
    @Autowired
    PortalFactory portalFactory;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Transactional
    public void testPopulateClean(TestInfo info) throws IOException {
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        String surveyFile = "portals/ourhealth/studies/ourheart/surveys/basic.json";
        PortalPopulateContext context =
                new PortalPopulateContext(surveyFile, portal.getShortcode(), null, new HashMap<>(), false, null);
        Survey freshSurvey = surveyPopulator.populate(context, false);
        checkSurvey(freshSurvey, "oh_oh_basicInfo", 1);

        Survey fetchedSurvey = surveyService.findByStableIdWithMappings("oh_oh_basicInfo", 1, portal.getId()).get();
        // check that answer mappings populate too
        assertThat(fetchedSurvey.getAnswerMappings().size(), greaterThan(0));

        List<SurveyQuestionDefinition> questionDefs = surveyQuestionDefinitionDao.findAllBySurveyId(fetchedSurvey.getId());
        assertThat(questionDefs, hasSize(54));
    }

    @Test
    @Transactional
    public void testPopulateOverride(TestInfo info) throws IOException {
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        String stableId = getTestName(info) + RandomStringUtils.randomAlphabetic(5);
        SurveyPopDto popDto1 = SurveyPopDto.builder()
                .stableId(stableId)
                .version(1)
                .jsonContent(objectMapper.readTree("{\"foo\": 12}"))
                .name("Survey 1").build();
        PortalPopulateContext context =
                new PortalPopulateContext("fake/file", portal.getShortcode(), null, new HashMap<>(), false, null);
        Survey newSurvey = surveyPopulator.populateFromDto(popDto1, context, true);
        checkSurvey(newSurvey, stableId, 1);

        SurveyPopDto popDto2 = SurveyPopDto.builder()
                .stableId(stableId)
                .version(1)
                .jsonContent(objectMapper.readTree("{\"foo\":17}"))
                .name("Survey 1").build();
        PortalPopulateContext context2 =
                new PortalPopulateContext("fake/file", portal.getShortcode(), null, new HashMap<>(), false, null);
        Survey overrideSurvey = surveyPopulator.populateFromDto(popDto2, context, true);
        // should override the previous survey, and so still be version 1
        checkSurvey(overrideSurvey, stableId, 1);
        Survey loadedSurvey = surveyService.findByStableId(stableId, 1, portal.getId()).get();
        assertThat(loadedSurvey.getContent(), equalTo("{\"foo\":17}"));
    }

    @Test
    @Transactional
    public void testPopulateNoOverride(TestInfo info) throws IOException {
        Portal portal = portalFactory.buildPersisted(getTestName(info));
        String stableId = getTestName(info) + RandomStringUtils.randomAlphabetic(5);
        SurveyPopDto popDto1 = SurveyPopDto.builder()
                .stableId(stableId)
                .version(1)
                .jsonContent(objectMapper.readTree("{\"foo\": 12}"))
                .name("Survey 1").build();
        PortalPopulateContext context =
                new PortalPopulateContext("fake/file", portal.getShortcode(), null, new HashMap<>(), false, null);
        Survey newSurvey = surveyPopulator.populateFromDto(popDto1, context, true);
        checkSurvey(newSurvey, stableId, 1);

        SurveyPopDto popDto2 = SurveyPopDto.builder()
                .stableId(stableId)
                .version(1)
                .jsonContent(objectMapper.readTree("{\"foo\":17}"))
                .name("Survey 1").build();
        PortalPopulateContext context2 =
                new PortalPopulateContext("fake/file", portal.getShortcode(), null, new HashMap<>(), false, null);
        Survey overrideSurvey = surveyPopulator.populateFromDto(popDto2, context, false);

        // should NOT override the previous survey, and so still be saved as version 2
        checkSurvey(overrideSurvey, stableId, 2);
        Survey loadedSurvey = surveyService.findByStableId(stableId, 2, portal.getId()).get();
        assertThat(loadedSurvey.getContent(), equalTo("{\"foo\":17}"));

        // prior survey should have no updates
        Survey loadedPrevSurvey = surveyService.findByStableId(stableId, 1, portal.getId()).get();
        assertThat(loadedPrevSurvey.getContent(), equalTo("{\"foo\":12}"));
    }

    private void checkSurvey(Survey survey, String expectedStabledId, int expectedVersion) {
        DaoTestUtils.assertGeneratedProperties(survey);
        assertThat(survey.getStableId(), equalTo(expectedStabledId));
        assertThat(survey.getVersion(), equalTo(expectedVersion));
    }


}
