package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.dao.dataimport.TimeShiftPopulateDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.model.dataimport.Import;
import bio.terra.pearl.core.model.dataimport.ImportItem;
import bio.terra.pearl.core.model.dataimport.ImportItemStatus;
import bio.terra.pearl.core.model.dataimport.ImportStatus;
import bio.terra.pearl.core.model.dataimport.ImportType;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.dataimport.ImportFileFormat;
import bio.terra.pearl.core.service.dataimport.ImportItemService;
import bio.terra.pearl.core.service.dataimport.ImportService;
import bio.terra.pearl.core.service.exception.internal.InternalServerException;
import bio.terra.pearl.core.service.export.formatters.module.EnrolleeFormatter;
import bio.terra.pearl.core.service.export.formatters.module.KitRequestFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ParticipantUserFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ProfileFormatter;
import bio.terra.pearl.core.service.export.formatters.module.SurveyFormatter;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import bio.terra.pearl.core.service.kit.KitRequestService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import bio.terra.pearl.core.service.survey.SurveyTaskDispatcher;
import bio.terra.pearl.core.service.workflow.EnrollmentService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskAssignDto;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import bio.terra.pearl.core.service.workflow.RegistrationService;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnrolleeImportService {

    ExportOptions IMPORT_OPTIONS_TSV = ExportOptions
            .builder()
            .stableIdsForOptions(true)
            .onlyIncludeMostRecent(true)
            .fileFormat(ExportFileFormat.TSV)
            .limit(null)
            .build();

    ExportOptions IMPORT_OPTIONS_CSV = ExportOptions
            .builder()
            .stableIdsForOptions(true)
            .onlyIncludeMostRecent(true)
            .fileFormat(ExportFileFormat.CSV)
            .limit(null)
            .build();

    private final RegistrationService registrationService;
    private final EnrolleeService enrolleeService;
    private final EnrolleeExportService enrolleeExportService;
    private final EnrollmentService enrollmentService;
    private final ProfileService profileService;
    private final SurveyResponseService surveyResponseService;
    private final SurveyTaskDispatcher surveyTaskDispatcher;
    private final ParticipantTaskService participantTaskService;
    private final ParticipantUserService participantUserService;
    private final PortalService portalService;
    private final PortalParticipantUserService portalParticipantUserService;
    private final TimeShiftPopulateDao timeShiftPopulateDao;
    private final ImportService importService;
    private final ImportItemService importItemService;
    private final KitRequestService kitRequestService;
    private final char CSV_DELIMITER = ',';
    private final char TSV_DELIMITER = '\t';

    public EnrolleeImportService(RegistrationService registrationService, EnrollmentService enrollmentService,
                                 ProfileService profileService, EnrolleeExportService enrolleeExportService,
                                 SurveyResponseService surveyResponseService, ParticipantTaskService participantTaskService, PortalService portalService,
                                 ImportService importService, ImportItemService importItemService, SurveyTaskDispatcher surveyTaskDispatcher,
                                 TimeShiftPopulateDao timeShiftPopulateDao, EnrolleeService enrolleeService, ParticipantUserService participantUserService,
                                 PortalParticipantUserService portalParticipantUserService, KitRequestService kitRequestService) {
        this.registrationService = registrationService;
        this.enrollmentService = enrollmentService;
        this.profileService = profileService;
        this.enrolleeExportService = enrolleeExportService;
        this.surveyResponseService = surveyResponseService;
        this.participantTaskService = participantTaskService;
        this.portalService = portalService;
        this.importService = importService;
        this.importItemService = importItemService;
        this.surveyTaskDispatcher = surveyTaskDispatcher;
        this.timeShiftPopulateDao = timeShiftPopulateDao;
        this.enrolleeService = enrolleeService;
        this.participantUserService = participantUserService;
        this.portalParticipantUserService = portalParticipantUserService;
        this.kitRequestService = kitRequestService;
    }

    @Transactional
    /**
     * imports the enrollees serialized in the inputstream to the given environment
     */
    public Import importEnrollees(String portalShortcode, String studyShortcode, StudyEnvironment studyEnv, InputStream in,
                                  UUID adminId, ImportFileFormat fileFormat) {
        Import dataImport = Import.builder()
                .responsibleUserId(adminId)
                .studyEnvironmentId(studyEnv.getId())
                .responsibleUserId(adminId)
                .importType(ImportType.PARTICIPANT)
                .status(ImportStatus.PROCESSING)
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .build();
        dataImport = importService.create(dataImport);
        log.info("Started Import ID: {}", dataImport.getId());

        ExportOptions exportOptions = IMPORT_OPTIONS_TSV;
        if (ImportFileFormat.CSV.equals(fileFormat)) {
            exportOptions = IMPORT_OPTIONS_CSV;
        }
        List<Map<String, String>> enrolleeMaps = generateImportMaps(in, fileFormat);
        for (Map<String, String> enrolleeMap : enrolleeMaps) {
            Enrollee enrollee = null;
            ImportItem importItem;
            try {
                enrollee = importEnrollee(portalShortcode, studyShortcode, studyEnv, enrolleeMap, exportOptions, adminId);
                importItem = ImportItem.builder()
                        .createdEnrolleeId(enrollee.getId())
                        .importId(dataImport.getId())
                        .createdParticipantUserId(enrollee.getParticipantUserId())
                        .createdAt(Instant.now())
                        .lastUpdatedAt(Instant.now())
                        .status(ImportItemStatus.SUCCESS).build();
            } catch (Exception e) {
                importItem = ImportItem.builder()
                        .importId(dataImport.getId())
                        .createdParticipantUserId(adminId)
                        .createdAt(Instant.now())
                        .lastUpdatedAt(Instant.now())
                        .status(ImportItemStatus.FAILED)
                        .message(e.getMessage())
                        .detail(Arrays.toString(e.getStackTrace())).build();
            }

            importItem = importItemService.create(importItem);
            log.debug("populated Import Item ID: {}", importItem.getId());
        }
        dataImport.setStatus(ImportStatus.DONE);
        importService.update(dataImport);
        importItemService.attachImportItems(dataImport);
        log.info("Completed importing : {} items for Import ID: {}", dataImport.getImportItems().size(), dataImport.getId());
        return dataImport;
    }

    /**
     * transforms a TSV import input stream into a List of string maps, one map per enrollee
     */
    public List<Map<String, String>> generateImportMaps(InputStream in, ImportFileFormat fileFormat) {
        List<Map<String, String>> importMaps = new ArrayList<>();
        char separator = TSV_DELIMITER;
        if (fileFormat == ImportFileFormat.CSV) {
            separator = CSV_DELIMITER;
        }
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(separator).build();
        try (CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(in)).withCSVParser(parser).build()) {
            String[] headers = csvReader.readNext();
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line[0].equalsIgnoreCase("shortcode")) {
                    //skip this sub header line
                    continue;
                }
                Map<String, String> enrolleeMap = new HashMap<>();
                for (int i = 0; i < line.length; i++) {
                    enrolleeMap.put(headers[i], line[i]);
                }
                importMaps.add(enrolleeMap);
            }
            return importMaps;
        } catch (IOException | CsvValidationException e) {
            throw new InternalServerException("error reading input stream", e);
        }
    }

    public Enrollee importEnrollee(String portalShortcode, String studyShortcode, StudyEnvironment studyEnv, Map<String, String> enrolleeMap, ExportOptions exportOptions, UUID adminId) {
        /** while importing handle update for existing import
         if same enrolle: update enrollee
         if same participant & same portal.. new enrollee & same profile
         if same participant & different portal.. new enrollee & new profile
         **/

        DataAuditInfo auditInfo = DataAuditInfo.builder().systemProcess(
                DataAuditInfo.systemProcessName(getClass(), "importEnrollee")
        ).build();

        ParticipantUserFormatter participantUserFormatter = new ParticipantUserFormatter(exportOptions);
        final ParticipantUser participantUserInfo = participantUserFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);

        final RegistrationService.RegistrationResult regResult = registerIfNeeded(portalShortcode, studyEnv, participantUserInfo);

        Enrollee enrollee = createEnrolleeIfNeeded(studyShortcode, studyEnv, enrolleeMap, exportOptions, regResult, auditInfo, participantUserInfo);

        /** now update the profile */
        Profile profile = importProfile(enrolleeMap, regResult.profile(), exportOptions, studyEnv, auditInfo);

        /** populate kit_requests */
        importKitRequests(enrolleeMap, adminId, enrollee);

        importSurveyResponses(portalShortcode, enrolleeMap, exportOptions, studyEnv, regResult.portalParticipantUser(), enrollee, auditInfo);

        /** restore email */
        profile.setDoNotEmail(false);
        profileService.update(profile, auditInfo);
        return enrollee;
    }

    private void importKitRequests(Map<String, String> enrolleeMap, UUID adminId, Enrollee enrollee) {
        List<KitRequest> kitRequests = new KitRequestFormatter().listFromStringMap(enrolleeMap).stream().map(
                kitRequestDto -> insertKitRequest(adminId, enrollee, kitRequestDto)).collect(Collectors.toList());

    }

    public KitRequest insertKitRequest(
            UUID adminUserId,
            Enrollee enrollee,
            KitRequestDto kitRequestDto) {

        KitType kitType = kitRequestService.lookupKitTypeByName(kitRequestDto.getKitType().getName());
        KitRequest kitRequest = KitRequest.builder()
                .creatingAdminUserId(adminUserId)
                .enrolleeId(enrollee.getId())
                .status(kitRequestDto.getStatus())
                .sentToAddress(kitRequestDto.getSentToAddress())
                .skipAddressValidation(kitRequestDto.isSkipAddressValidation())
                .kitTypeId(kitType.getId())
                .createdAt(kitRequestDto.getCreatedAt())
                .labeledAt(kitRequestDto.getLabeledAt())
                .sentAt(kitRequestDto.getSentAt())
                .receivedAt(kitRequestDto.getReceivedAt())
                .trackingNumber(kitRequestDto.getTrackingNumber())
                .returnTrackingNumber(kitRequestDto.getReturnTrackingNumber())
                .build();

        return kitRequestService.create(kitRequest);
    }

    private RegistrationService.RegistrationResult registerIfNeeded(String portalShortcode, StudyEnvironment studyEnv, ParticipantUser participantUserInfo) {
        return portalParticipantUserService.findOne(participantUserInfo.getUsername(), portalShortcode, studyEnv.getEnvironmentName())
                .map(ppUser -> new RegistrationService.RegistrationResult(
                        participantUserService.findOne(participantUserInfo.getUsername(),
                                studyEnv.getEnvironmentName()).orElseThrow(() -> new IllegalStateException("Participant User could not be found or for PPUser")),
                        ppUser,
                        profileService.find(ppUser.getProfileId()).orElseThrow(IllegalStateException::new)
                )).orElseGet(() ->
                        registrationService.register(portalShortcode, studyEnv.getEnvironmentName(), participantUserInfo.getUsername(), null, null)
                );
    }

    private @NotNull Enrollee createEnrolleeIfNeeded(String studyShortcode, StudyEnvironment studyEnv, Map<String, String> enrolleeMap, ExportOptions exportOptions,
                                                     RegistrationService.RegistrationResult regResult, DataAuditInfo auditInfo, ParticipantUser participantUserInfo) {
        return enrolleeService.findByParticipantUserIdAndStudyEnvId(regResult.participantUser().getId(), studyEnv.getId()).orElseGet(() -> {
            /** user is not enrolled in this study, so we need to create a new enrollee */
            EnrolleeFormatter enrolleeFormatter = new EnrolleeFormatter(exportOptions);
            Enrollee enrolleeInfo = enrolleeFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);
            /** temporarily update the profile to no emails since they'll receive a special welcome email */
            regResult.profile().setDoNotEmail(true);
            profileService.update(regResult.profile(), auditInfo);

            HubResponse<Enrollee> response = enrollmentService.enroll(regResult.portalParticipantUser(), studyEnv.getEnvironmentName(),
                    studyShortcode, regResult.participantUser(), regResult.portalParticipantUser(), null, enrolleeInfo.isSubject());
            Enrollee newEnrollee = response.getEnrollee();
            //update createdAt
            if (newEnrollee.getCreatedAt() != null) {
                timeShiftPopulateDao.changeEnrolleeCreationTime(response.getEnrollee().getId(), enrolleeInfo.getCreatedAt());
            }
            if (regResult.participantUser().getCreatedAt() != null) {
                timeShiftPopulateDao.changeParticipantAccountCreationTime(response.getEnrollee().getParticipantUserId(), participantUserInfo.getCreatedAt());
            }
            return newEnrollee;
        });
    }

    protected Profile importProfile(Map<String, String> enrolleeMap, Profile registrationProfile,
                                    ExportOptions exportOptions, StudyEnvironment studyEnv, DataAuditInfo auditInfo) {
        ProfileFormatter profileFormatter = new ProfileFormatter(exportOptions);
        Profile profile = profileFormatter.fromStringMap(studyEnv.getId(), enrolleeMap);
        profile.setId(registrationProfile.getId());
        profile.setMailingAddressId(registrationProfile.getMailingAddressId());
        profile.getMailingAddress().setId(registrationProfile.getMailingAddressId());
        return profileService.updateWithMailingAddress(profile, auditInfo);
    }

    protected List<SurveyResponse> importSurveyResponses(String portalShortcode,
                                                         Map<String, String> enrolleeMap,
                                                         ExportOptions exportOptions,
                                                         StudyEnvironment studyEnv,
                                                         PortalParticipantUser ppUser,
                                                         Enrollee enrollee,
                                                         DataAuditInfo auditInfo) {
        List<SurveyFormatter> surveyModules = enrolleeExportService.generateSurveyModules(exportOptions, studyEnv.getId());
        List<SurveyResponse> responses = new ArrayList<>();
        UUID portalId = portalService.findOneByShortcode(portalShortcode).orElseThrow().getId();
        for (SurveyFormatter formatter : surveyModules) {
            SurveyResponse surveyResponse = importSurveyResponse(portalId, formatter, enrolleeMap, exportOptions, studyEnv, ppUser, enrollee, auditInfo);
            if (surveyResponse != null) {
                responses.add(surveyResponse);
            }
        }
        return responses;
    }

    protected SurveyResponse importSurveyResponse(UUID portalId, SurveyFormatter formatter, Map<String, String> enrolleeMap, ExportOptions exportOptions,
                                                  StudyEnvironment studyEnv, PortalParticipantUser ppUser, Enrollee enrollee, DataAuditInfo auditInfo) {
        SurveyResponse response = formatter.fromStringMap(studyEnv.getId(), enrolleeMap);
        if (response == null) {
            return null;
        }
        ParticipantTask relatedTask = participantTaskService.findTaskForActivity(ppUser.getId(), studyEnv.getId(), formatter.getModuleName())
                .orElse(null);
        if (relatedTask == null) {
            ParticipantTaskAssignDto assignDto = new ParticipantTaskAssignDto(
                    TaskType.SURVEY,
                    formatter.getModuleName(),
                    1,
                    null,
                    true,
                    true);

            List<ParticipantTask> tasks = surveyTaskDispatcher.assign(assignDto, studyEnv.getId(),
                    new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "handleSurveyPublished.assignToExistingEnrollees")));
            relatedTask = tasks.get(0);
        }
        // we're not worrying about dating the response yet
        return surveyResponseService.updateResponse(response, new ResponsibleEntity(DataAuditInfo.systemProcessName(getClass(), "importSurveyResponse")),
                ppUser, enrollee, relatedTask.getId(), portalId).getResponse();
    }
}
