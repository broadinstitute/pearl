package bio.terra.pearl.core.dao.participant;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.consent.ConsentResponseDao;
import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.dao.survey.SurveyResponseDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class EnrolleeDao extends BaseMutableJdbiDao<Enrollee> {
    private final ConsentResponseDao consentResponseDao;
    private final KitRequestDao kitRequestDao;
    private final KitTypeDao kitTypeDao;
    private final ParticipantTaskDao participantTaskDao;
    private final PreEnrollmentResponseDao preEnrollmentResponseDao;
    private final ProfileDao profileDao;
    private final SurveyResponseDao surveyResponseDao;

    public EnrolleeDao(Jdbi jdbi,
                       ConsentResponseDao consentResponseDao,
                       KitRequestDao kitRequestDao,
                       KitTypeDao kitTypeDao,
                       ParticipantTaskDao participantTaskDao,
                       PreEnrollmentResponseDao preEnrollmentResponseDao,
                       ProfileDao profileDao,
                       SurveyResponseDao surveyResponseDao) {
        super(jdbi);
        this.consentResponseDao = consentResponseDao;
        this.kitRequestDao = kitRequestDao;
        this.kitTypeDao = kitTypeDao;
        this.participantTaskDao = participantTaskDao;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.profileDao = profileDao;
        this.surveyResponseDao = surveyResponseDao;
    }

    @Override
    protected Class<Enrollee> getClazz() {
        return Enrollee.class;
    }

    public Optional<Enrollee> findOneByShortcode(String shortcode) {
        return findByProperty("shortcode", shortcode);
    }

    public List<Enrollee> findByStudyEnvironmentId(UUID studyEnvironmentId) {
        return findAllByProperty("study_environment_id", studyEnvironmentId);
    }

    public List<Enrollee> findAllByShortcodes(List<String> shortcodes) {
        return findAllByPropertyCollection("shortcode", shortcodes);
    }

    public List<Enrollee> findByParticipantUserId(UUID userId) {
        return findAllByProperty("participant_user_id", userId);
    }

    public List<Enrollee> findByProfileId(UUID profileId) {
        return findAllByProperty("profile_id", profileId);
    }

    public Optional<Enrollee> findByParticipantUserId(UUID userId, UUID studyEnvironmentId) {
        return findByTwoProperties("participant_user_id", userId,
                "study_environment_id", studyEnvironmentId);
    }

    public Optional<Enrollee> findByEnrolleeId(UUID userId, UUID enrolleeId) {
        return findByTwoProperties("participant_user_id", userId, "id", enrolleeId);
    }

    public Optional<Enrollee> findByEnrolleeId(UUID userId, String enrolleeShortcode) {
        return findByTwoProperties("participant_user_id", userId, "shortcode", enrolleeShortcode);
    }

    public Optional<Enrollee> findByPreEnrollResponseId(UUID preEnrollResponseId) {
        return findByProperty("pre_enrollment_response_id", preEnrollResponseId);
    }

    /** loads child relationships including survey responses, profile, etc... */
    public Enrollee loadForAdminView(Enrollee enrollee) {
        enrollee.getSurveyResponses().addAll(surveyResponseDao.findByEnrolleeIdWithAnswers(enrollee.getId()));
        enrollee.getConsentResponses().addAll(consentResponseDao.findByEnrolleeId(enrollee.getId()));
        enrollee.setProfile(profileDao.loadWithMailingAddress(enrollee.getProfileId()).get());
        enrollee.getParticipantTasks().addAll(participantTaskDao.findByEnrolleeId(enrollee.getId()));
        if (enrollee.getPreEnrollmentResponseId() != null) {
            enrollee.setPreEnrollmentResponse(preEnrollmentResponseDao.find(enrollee.getPreEnrollmentResponseId()).get());
        }
        enrollee.getKitRequests().addAll(kitRequestDao.findByEnrollee(enrollee.getId()));
        var allKitTypes = kitTypeDao.findAll();
        for (KitRequest kitRequest : enrollee.getKitRequests()) {
            var kitType = allKitTypes.stream().filter((t -> t.getId().equals(kitRequest.getKitTypeId()))).findFirst().get();
            kitRequest.setKitType(kitType);
        }
        return enrollee;
    }

    public int countByStudyEnvironment(UUID studyEnvironmentId) {
        return countByProperty("study_environment_id", studyEnvironmentId);
    }
    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        deleteByProperty("study_environment_id", studyEnvironmentId);
    }

    /** updates the global consent status of the enrollee */
    public void updateConsented(UUID enrolleeId, boolean consented) {
        updateProperty(enrolleeId, "consented", consented);
    }
}
