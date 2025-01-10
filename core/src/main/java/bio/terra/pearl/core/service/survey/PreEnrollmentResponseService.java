package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PreEnrollmentResponseService extends CrudService<PreEnrollmentResponse, PreEnrollmentResponseDao> {
    public PreEnrollmentResponseService(PreEnrollmentResponseDao dao) {
        super(dao);
    }

    public Map<UUID, PreEnrollmentResponse> findByParticipantUserIds(Collection<UUID> participantUserIds) {
        return dao.findByParticipantUserIds(participantUserIds);
    }
}
