package bio.terra.pearl.core.dao.survey;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import bio.terra.pearl.core.service.survey.PreEnrollmentResponseService;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

@Component
public class PreEnrollmentResponseDao extends BaseMutableJdbiDao<PreEnrollmentResponse> {
    public PreEnrollmentResponseDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<PreEnrollmentResponse> getClazz() {
        return PreEnrollmentResponse.class;
    }

    public void deleteByStudyEnvironmentId(UUID studyEnvironmentId) {
        deleteByProperty("study_environment_id", studyEnvironmentId);
    }

    public Map<UUID, PreEnrollmentResponse> findByParticipantUserIds(Collection<UUID> participantUserIds) {
        return findAllByPropertyCollection("creating_participant_user_id", participantUserIds)
                .stream().collect(Collectors.toMap(
                        PreEnrollmentResponse::getCreatingParticipantUserId,
                        response -> response,
                        (existing, replacement) -> existing
                )); //todo
    }
}
