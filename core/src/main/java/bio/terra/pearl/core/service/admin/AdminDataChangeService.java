package bio.terra.pearl.core.service.admin;

import bio.terra.pearl.core.dao.admin.AdminDataChangeDao;
import bio.terra.pearl.core.model.admin.AdminDataChange;
import bio.terra.pearl.core.service.ImmutableEntityService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminDataChangeService extends ImmutableEntityService<AdminDataChange, AdminDataChangeDao> {
    public AdminDataChangeService(AdminDataChangeDao dao) {
        super(dao);
    }

    public void deleteByPortalId(UUID portalId) {
        dao.deleteByPortalId(portalId);
    }
}
