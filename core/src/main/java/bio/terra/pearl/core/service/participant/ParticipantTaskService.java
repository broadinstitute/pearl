package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.dao.participant.ParticipantTaskDao;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.service.CrudService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipantTaskService extends CrudService<ParticipantTask, ParticipantTaskDao> {
    public ParticipantTaskService(ParticipantTaskDao dao) {
        super(dao);
    }

    public List<ParticipantTask> findByEnrolleeId(UUID enrolleeId) {
        return dao.findByEnrolleeId(enrolleeId);
    }

    public void deleteByEnrolleeId(UUID enrolleeId) { dao.deleteByEnrolleeId(enrolleeId);}

    @Transactional
    public ParticipantTask updateTaskStatus(UUID taskId, TaskStatus newStatus) {
        return dao.updateTaskStatus(taskId, newStatus);
    }
}
