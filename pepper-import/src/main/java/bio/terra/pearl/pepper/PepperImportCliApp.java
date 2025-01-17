package bio.terra.pearl.pepper;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.exception.internal.IOInternalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CLI importer -- see the "run" method for argument descriptions.
 */
@SpringBootApplication(scanBasePackages = {"bio.terra.pearl.core", "bio.terra.pearl.populate", "bio.terra.pearl.pepper"})
@Slf4j
public class PepperImportCliApp implements CommandLineRunner {

    public static final String ABSOLUTE_SEED_ROOT = "pepper-import/src/main/resources/pepper";
    public static final String OUTPUT_ROOT = "pepper-import/out";

    public static void main(String[] args) {
        SpringApplication.run(PepperImportCliApp.class, args);
    }

    public static String SUBSTITUTIONS_FILE = "substitutions.conf";

    @Autowired
    private ActivityImporter activityImporter;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        log.info("EXECUTING : command line importer");
        // these vars should be read from command line or a conf file eventually
        String studyDir = "atcp";
        String[] formFiles = {"prequal.conf", "registration.conf", "self-consent.conf", "self-consent-edit.conf", "assent.conf",
                "medical-history.conf", "contacting-physician.conf", "review-and-submission.conf", "genome-study.conf", "blood-type.conf", "stay-informed.conf"};

        Config varsCfg = ConfigFactory.parseFile(getFilePath("studies/%s/%s".formatted(studyDir, SUBSTITUTIONS_FILE), ABSOLUTE_SEED_ROOT).toFile());

        for (String formFile : formFiles) {
            Survey survey = activityImporter.parsePepperForm(varsCfg, getFilePath("studies/" + studyDir, ABSOLUTE_SEED_ROOT), Path.of(formFile));
            try {
                Path outFilePath = getFilePath(formFile, OUTPUT_ROOT);
                String surveyString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(survey);
                Files.write(outFilePath, surveyString.getBytes());
            } catch (IOException e) {
                throw new IOInternalException("couldn't write survey", e);
            }
        }
        log.info("COMPLETE : command line importer");
    }


    /**
     * Get the absolute path for a file.
     * depending on whether you are running gradle or spring boot, the root directory could either be
     * the root folder or pepper-import.  So strip out pepper-import if it's there
     */
    public static Path getFilePath(String path, String root) {

        String projectDir = System.getProperty("user.dir").replace("/pepper-import", "");
        String pathName = projectDir + "/" + root + "/" + path;
        return Path.of(pathName);
    }
}
