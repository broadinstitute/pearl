package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.EnrolleeRelationDao;
import bio.terra.pearl.core.model.audit.DataAuditInfo;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.service.DataAuditedService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.workflow.DataChangeRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EnrolleeRelationService extends DataAuditedService<EnrolleeRelation, EnrolleeRelationDao> {
    private final EnrolleeService enrolleeService;
    private final ProfileService profileService;
    public EnrolleeRelationService(EnrolleeRelationDao enrolleeRelationDao,
                                   DataChangeRecordService dataChangeRecordService,
                                   @Lazy EnrolleeService enrolleeService,
                                   ObjectMapper objectMapper,
                                   ProfileService profileService) {
        super(enrolleeRelationDao, dataChangeRecordService, objectMapper);
        this.enrolleeService = enrolleeService;
        this.profileService = profileService;
    }

    public List<EnrolleeRelation> findByEnrolleeIdAndRelationType(UUID enrolleeId, RelationshipType relationshipType) {
        return filterValid(dao.findByEnrolleeIdAndRelationshipType(enrolleeId, relationshipType));
    }

    public List<EnrolleeRelation> findByEnrolleeIdsAndRelationType(List<UUID> enrolleeIds, RelationshipType relationshipType) {
        return filterValid(dao.findByEnrolleeIdsAndRelationshipType(enrolleeIds, relationshipType));
    }

    public List<EnrolleeRelation> findByTargetEnrolleeId(UUID enrolleeId) {
        return filterValid(dao.findByTargetEnrolleeId(enrolleeId));
    }

    public List<EnrolleeRelation> findByTargetEnrolleeIdWithEnrollees(UUID enrolleeId) {
        Enrollee target = this.enrolleeService.find(enrolleeId).orElseThrow(() -> new NotFoundException("Enrollee not found"));
        profileService
                .loadWithMailingAddress(target.getProfileId())
                .ifPresent(target::setProfile);

        List<EnrolleeRelation> relations = findByTargetEnrolleeId(enrolleeId);

        return relations.stream().map(relation -> {
            relation.setTargetEnrollee(target);
            relation.setEnrollee(this.enrolleeService.find(relation.getEnrolleeId()).orElse(null));
            profileService
                    .loadWithMailingAddress(relation.getEnrollee().getProfileId())
                    .ifPresent(relation.getEnrollee()::setProfile);
            return relation;
        }).toList();
    }

    public boolean isUserProxyForAnyOf(UUID participantUserId, List<UUID> enrolleeIds) {
        return !dao.findEnrolleeRelationsByProxyParticipantUser(participantUserId, enrolleeIds)
                .stream().filter(enrolleeRelation -> isRelationshipValid(enrolleeRelation)).collect(Collectors.toList()).isEmpty();
    }

    public Optional<Enrollee> isUserProxyForEnrollee(UUID participantUserId, String enrolleeShortcode) {
        Enrollee targetEnrollee = enrolleeService.findOneByShortcode(enrolleeShortcode)
                .orElseThrow(() -> new NotFoundException("Enrollee with shortcode %s was not found ".formatted(enrolleeShortcode)));
        List<EnrolleeRelation> relations = dao.findEnrolleeRelationsByProxyParticipantUser(participantUserId, List.of(targetEnrollee.getId()));
        if (!validRelations(relations).isEmpty()) {
            return Optional.of(targetEnrollee);
        }
        return Optional.empty();
    }

    private List<EnrolleeRelation> validRelations(List<EnrolleeRelation> in) {
        return in.stream().filter(this::isRelationshipValid).toList();
    }

    public void attachTargetEnrollees(List<EnrolleeRelation> relations) {
        dao.attachTargetEnrollees(relations);
    }


    public boolean isRelationshipValid(EnrolleeRelation enrolleeRelation) {
        return (enrolleeRelation.getEndDate() == null || enrolleeRelation.getEndDate().isAfter(Instant.now()));
    }

    public void deleteAllByEnrolleeIdOrTargetId(UUID enrolleeId) {
        List<EnrolleeRelation> enrolleeRelations = dao.findAllByEnrolleeId(enrolleeId);
        enrolleeRelations.addAll(dao.findByTargetEnrolleeId(enrolleeId));
        bulkDelete(enrolleeRelations, DataAuditInfo.builder().systemProcess(DataAuditInfo.systemProcessName(getClass(),
                "deleteAllByEnrolleeIdOrTargetId")).build());
    }

    /**
     * This method returns a list of enrollees that are exclusively proxied by the given enrollee.
     * An enrollee is exclusively proxied if it is only proxied by the given enrollee and no other enrollees.
     * @param enrolleeId the id of the enrollee to find exclusive proxied enrollees for
     * @return a list of enrollees that are exclusively proxied by the given enrollee
     * */
    public List<Enrollee> findExclusiveProxiedEnrollees(UUID enrolleeId) {
        List<EnrolleeRelation> enrolleeRelations = dao.findByEnrolleeIdAndRelationshipType(enrolleeId, RelationshipType.PROXY);
        List<Enrollee> exclusiveGovernedEnrollees = new ArrayList<>();
        for (EnrolleeRelation enrolleeRelation : enrolleeRelations) {
            if (findByTargetEnrolleeId(enrolleeRelation.getTargetEnrolleeId()).size() == 1) {
                enrolleeService.find(enrolleeRelation.getTargetEnrolleeId()).ifPresent(exclusiveGovernedEnrollees::add);
            }
        }
        return exclusiveGovernedEnrollees;
    }

    public List<EnrolleeRelation> filterValid(List<EnrolleeRelation> enrolleeRelations) {
        return enrolleeRelations.stream().filter(this::isRelationshipValid).collect(Collectors.toList());
    }

}
