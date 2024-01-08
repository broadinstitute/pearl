package bio.terra.pearl.populate.extract;

import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.populate.BasePopulatePortalsTest;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.extract.PortalExtractService;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class PortalExtractTest extends BasePopulatePortalsTest {

    @Autowired
    private PortalExtractService portalExtractService;

    @Test
    @Transactional
    public void testExtractOurHealth() throws Exception {
        // populate a portal, then see if we can extract it, delete it, and repopulate it

        setUpEnvironments();
        Portal portal = portalPopulator.populate(new FilePopulateContext("portals/ourhealth/portal.json"), true);
        String tmpFileName = "/tmp/ourhealth-%s.zip".formatted(RandomStringUtils.randomAlphanumeric(8));
        File tmpFile = new File(tmpFileName);
        FileOutputStream fos = new FileOutputStream(tmpFile);
        portalExtractService.extract("ourhealth", fos);
        fos.close();

        // we technically don't need this manual delete since the populate below should include a delete, but just to be sure...
        portalService.delete(portal.getId(), Set.of(PortalService.AllowedCascades.STUDY));

        ZipInputStream zis = new ZipInputStream(new FileInputStream(tmpFileName));
        Portal restoredPortal = portalPopulator.populateFromZipFile(zis, true);

        // confirm all templates got repopulated
        assertThat(surveyService.findByPortalId(restoredPortal.getId()), hasSize(9));
        assertThat(studyService.findByPortalId(restoredPortal.getId()), hasSize(1));
        assertThat(consentFormService.findByPortalId(restoredPortal.getId()), hasSize(1));
        assertThat(emailTemplateService.findByPortalId(restoredPortal.getId()), hasSize(5));

        // confirm the sandbox got configured
        Study study = studyService.findByPortalId(restoredPortal.getId()).get(0);
        StudyEnvironment sandboxEnv = studyEnvironmentService.findByStudy(study.getShortcode(), EnvironmentName.sandbox).orElseThrow();
        assertThat(notificationConfigService.findByStudyEnvironmentId(sandboxEnv.getId()), hasSize(5));
    }


}
