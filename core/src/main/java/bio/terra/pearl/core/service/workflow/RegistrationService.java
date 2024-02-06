package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.dao.survey.PreregistrationResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.survey.ParsedPreRegResponse;
import bio.terra.pearl.core.model.survey.PreregistrationResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ParticipantUtilService;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.survey.AnswerProcessingService;
import bio.terra.pearl.core.service.survey.SurveyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RegistrationService {
    private SurveyService surveyService;
    private ParticipantUtilService participantUtilService;
    private PortalEnvironmentService portalEnvService;
    private PreregistrationResponseDao preregistrationResponseDao;
    private AnswerProcessingService answerProcessingService;
    private ParticipantUserService participantUserService;
    private PortalParticipantUserService portalParticipantUserService;
    private EventService eventService;
    private ObjectMapper objectMapper;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String GOVERNED_USER_INDICATOR = "-proxied-";
    private static final int LENGTH = 10;
    private final Random random = new SecureRandom();

    public RegistrationService(SurveyService surveyService,
                               PortalEnvironmentService portalEnvService,
                               PreregistrationResponseDao preregistrationResponseDao,
                               AnswerProcessingService answerProcessingService,
                               ParticipantUserService participantUserService,
                               PortalParticipantUserService portalParticipantUserService,
                               EventService eventService, ObjectMapper objectMapper,
                               ParticipantUtilService participantUtilService) {
        this.surveyService = surveyService;
        this.portalEnvService = portalEnvService;
        this.preregistrationResponseDao = preregistrationResponseDao;
        this.answerProcessingService = answerProcessingService;
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.eventService = eventService;
        this.objectMapper = objectMapper;
        this.participantUtilService = participantUtilService;
    }

    /**
     * Creates a preregistration survey record for a user who is not signed in
     */
    @Transactional
    public PreregistrationResponse createAnonymousPreregistration(
            String portalShortcode,
            EnvironmentName envName,
            String surveyStableId,
            Integer surveyVersion,
            ParsedPreRegResponse parsedResponse) throws JsonProcessingException {
        PreregistrationResponse response = new PreregistrationResponse();
        Survey survey = surveyService.findByStableId(surveyStableId, surveyVersion).get();
        PortalEnvironment portalEnv = portalEnvService.findOne(portalShortcode, envName).get();

        response.setSurveyId(survey.getId());
        response.setPortalEnvironmentId(portalEnv.getId());
        response.setFullData(objectMapper.writeValueAsString(parsedResponse.getAnswers()));
        return preregistrationResponseDao.create(response);
    }

    public Optional<PreregistrationResponse> find(UUID preRegResponseId) {
        return preregistrationResponseDao.find(preRegResponseId);
    }

    @Transactional
    public RegistrationResult register(String portalShortcode, EnvironmentName environmentName,
                                       String email, UUID preRegResponseId) {
        RequiredRegistrationInfo info = RequiredRegistrationInfo.builder().email(email).build();
        return register(portalShortcode, environmentName, preRegResponseId, info);
    }

    private RegistrationResult register(String portalShortcode, EnvironmentName environmentName, UUID preRegResponseId,
                                        RequiredRegistrationInfo info) {
        PortalEnvironment portalEnv = portalEnvService.findOne(portalShortcode, environmentName).get();
        PreregistrationResponse preRegResponse = null;
        if (portalEnv.getPreRegSurveyId() != null) {
            preRegResponse = validatePreRegResponseId(preRegResponseId);
        }

        Optional<ParticipantUser> existingUserOpt = participantUserService.findOne(info.email, environmentName);
        ParticipantUser user = existingUserOpt.orElseGet(() -> {
            ParticipantUser newUser = new ParticipantUser();
            newUser.setEnvironmentName(environmentName);
            newUser.setUsername(info.email);
            return participantUserService.create(newUser);
        });

        PortalParticipantUser ppUser = new PortalParticipantUser();
        ppUser.setPortalEnvironmentId(portalEnv.getId());
        ppUser.setParticipantUserId(user.getId());

        Profile profile = new Profile();
        profile.setContactEmail(info.getEmail());
        profile.setGivenName(info.getFirstName());
        profile.setFamilyName(info.getLastName());
        ppUser.setProfile(profile);

        ppUser = portalParticipantUserService.create(ppUser);
        if (preRegResponse != null) {
            preRegResponse.setPortalParticipantUserId(ppUser.getId());
            preregistrationResponseDao.update(preRegResponse);
        }
        eventService.publishPortalRegistrationEvent(user, ppUser, portalEnv);
        log.info("Portal registration: userId: {}, portal: {}", user.getId(), portalShortcode);
        return new RegistrationResult(user, ppUser);
    }

    public RegistrationResult registerGovernedUser(String portalShortcode, ParticipantUser proxy) {
        PortalEnvironment portalEnv = portalEnvService.findOne(portalShortcode, proxy.getEnvironmentName()).get();
        ParticipantUser governedUser = new ParticipantUser();
        governedUser.setEnvironmentName(proxy.getEnvironmentName());
        String guid = generateGUID(governedUser.getUsername(), proxy.getEnvironmentName());
        String governedUserName = proxy.getUsername() + GOVERNED_USER_INDICATOR + guid;
        governedUser.setUsername(governedUserName);
        governedUser = participantUserService.create(governedUser);

        PortalParticipantUser ppUser = new PortalParticipantUser();
        ppUser.setPortalEnvironmentId(portalEnv.getId());
        ppUser.setParticipantUserId(governedUser.getId());

        Profile profile = new Profile();
        profile.setContactEmail(governedUser.getUsername());
        profile.setGivenName(null);
        profile.setFamilyName(null);
        ppUser.setProfile(profile);

        ppUser = portalParticipantUserService.create(ppUser);

        eventService.publishPortalRegistrationEvent(governedUser, ppUser, portalEnv);
        log.info("Governed user registration: userId: {}, portal: {}", governedUser.getId(), portalShortcode);
        return new RegistrationResult(governedUser, ppUser);
    }

    protected PreregistrationResponse validatePreRegResponseId(UUID preRegResponseId) {
        if (preRegResponseId == null) {
            throw new IllegalArgumentException("Preregistration response was not provided");
        }
        Optional<PreregistrationResponse> preRegResponseOpt = preregistrationResponseDao.find(preRegResponseId);
        if (preRegResponseOpt.isEmpty()) {
            throw new IllegalArgumentException("Preregistration response id was not valid");
        }
        PreregistrationResponse preRegResponse = preRegResponseOpt.get();
        if (preRegResponse.getPortalParticipantUserId() != null) {
            throw new IllegalArgumentException("Preregistration response has already been registered");
        }
        return preRegResponse;
    }

    public String generateGUID(String userName, EnvironmentName environmentName) {
        String guid = participantUtilService.generateSecureRandomString(LENGTH, CHARACTERS);
        while(!participantUserService.findOne(userName + guid, environmentName).isEmpty()){
            guid = participantUtilService.generateSecureRandomString(LENGTH, CHARACTERS);
        }
        return guid;
    }

    public record RegistrationResult(ParticipantUser participantUser,
                                     PortalParticipantUser portalParticipantUser) {
    }

    @SuperBuilder
    @NoArgsConstructor
    public static class RequiredRegistrationInfo {
        @Getter
        @Setter
        private String firstName;
        @Getter
        @Setter
        private String lastName;
        @Getter
        @Setter
        private String email;
    }


    /** we'll likely want to move this into the database at some point along with the registration survey */
    public static final Map<String, String> REGISTRATION_FIELD_MAP = Map.of(
            "reg_firstName", "firstName",
            "reg_lastName", "lastName",
            "reg_email", "email");
}
