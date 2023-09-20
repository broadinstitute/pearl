package bio.terra.pearl.core.dao.study;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.survey.SurveyDao;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class StudyEnvironmentSurveyDao extends BaseMutableJdbiDao<StudyEnvironmentSurvey> {
    private SurveyDao surveyDao;
    public StudyEnvironmentSurveyDao(Jdbi jdbi, SurveyDao surveyDao) {
        super(jdbi);
        this.surveyDao = surveyDao;
    }

    @Override
    protected Class<StudyEnvironmentSurvey> getClazz() {
        return StudyEnvironmentSurvey.class;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvId) {
        deleteByProperty("study_environment_id", studyEnvId);
    }

    public List<StudyEnvironmentSurvey> findAllByStudyEnvironmentId(UUID studyEnvId) {
        return findAllByPropertySorted("study_environment_id", studyEnvId,
                "survey_order", "ASC");
    }

    /** gets all the study environment surveys and attaches the relevant survey objects in a batch */
    public List<StudyEnvironmentSurvey> findAllByStudyEnvIdWithSurvey(UUID studyEnvId) {
        List<StudyEnvironmentSurvey> studyEnvSurvs = findAllByStudyEnvironmentId(studyEnvId);
        List<UUID> surveyIds = studyEnvSurvs.stream().map(ses -> ses.getSurveyId()).collect(Collectors.toList());
        List<Survey> surveys = surveyDao.findAll(surveyIds);
        for (StudyEnvironmentSurvey ses : studyEnvSurvs) {
            ses.setSurvey(surveys.stream().filter(survey -> survey.getId().equals(ses.getSurveyId()))
                    .findFirst().get());
        }
        return studyEnvSurvs;
    }

    public List<StudyEnvironmentSurvey> findAllStudyEnvsWithSurveyId(UUID surveyId) {
        return findAllByProperty("survey_id", surveyId);
    }

    /** finds by a surveyId and studyEnvironment */
    public Optional<StudyEnvironmentSurvey> findBySurvey(UUID studyEnvId, UUID surveyId) {
        return findByTwoProperties("study_environment_id",studyEnvId,
                "survey_id", surveyId);
    }

    public List<StudyEnvironmentSurvey> findBySurvey(UUID studyEnvId, String surveyStableId) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + prefixedGetQueryColumns("a") + " from " + tableName +
                                " a join survey on survey.id = a.survey_id" +
                                " where survey.stable_id = :stableId " +
                                " and a.study_environment_id = :studyEnvId;")
                        .bind("stableId", surveyStableId)
                        .bind("studyEnvId", studyEnvId)
                        .mapTo(clazz)
                        .list()
        );
    }
}
