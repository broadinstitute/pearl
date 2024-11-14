package bio.terra.pearl.core.dao.export;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.dao.StudyEnvAttachedDao;
import bio.terra.pearl.core.model.export.ExportIntegration;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExportIntegrationDao extends BaseMutableJdbiDao<ExportIntegration> implements StudyEnvAttachedDao<ExportIntegration> {
    private final ExportOptionsDao exportOptionsDao;
    public ExportIntegrationDao(Jdbi jdbi, ExportOptionsDao exportOptionsDao) {
        super(jdbi);
        this.exportOptionsDao = exportOptionsDao;
    }

    @Override
    protected Class<ExportIntegration> getClazz() {
        return ExportIntegration.class;
    }

    public List<ExportIntegration> findAllActiveWithOptions() {
        return findAllByPropertyWithChildren("enabled", true
                , "exportOptionsId", "exportOptions", exportOptionsDao);
    }
}
