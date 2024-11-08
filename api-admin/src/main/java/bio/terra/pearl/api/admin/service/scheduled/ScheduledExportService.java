package bio.terra.pearl.api.admin.service.scheduled;

import bio.terra.pearl.core.model.audit.ResponsibleEntity;
import bio.terra.pearl.core.service.datarepo.DataRepoExportService;
import bio.terra.pearl.core.service.export.integration.ExportIntegrationService;
import com.google.common.collect.ImmutableSet;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScheduledExportService {
  private final ExportIntegrationService exportIntegrationService;

  public ScheduledExportService(
          ExportIntegrationService exportIntegrationService) {
    this.exportIntegrationService = exportIntegrationService;
  }


  /**
   * Run at 1:00AM once per day. We're _very_ generous with lockAtMostFor
   * because this only runs once per day and we expect the process will take a while.
   */
  @Scheduled(cron = "0 0 1 * * *")
  @SchedulerLock(
          name = "ScheduledExportService.runScheduledExports",
          lockAtLeastFor = "1m",
          lockAtMostFor = "360m")
  public void runExportIntegrations() {
    log.info("Running export integrations");
    exportIntegrationService.doAllExports(new ResponsibleEntity("ScheduledExportService.runExportIntegrations"));
    log.info("Finished export integrations.");
  }
}
