package bio.terra.pearl.core.service.export.formatters;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.kit.KitRequestStatus;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.module.KitRequestFormatter;
import bio.terra.pearl.core.service.export.formatters.module.PreEnrollmentResponseFormatter;
import bio.terra.pearl.core.service.kit.KitRequestDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PreEnrollmentResponseFormatterTests {
    @Test
    public void testToStringMap() {
        PreEnrollmentResponse preEnrollmentResponse1 = PreEnrollmentResponse.builder()
                .surveyId(UUID.randomUUID())
                .referralSource("{ \"referringSite\": \"broadinstitute.org\"}")
                .build();
        PreEnrollmentResponseFormatter moduleFormatter = new PreEnrollmentResponseFormatter(new ExportOptions());
        EnrolleeExportData exportData = new EnrolleeExportData(null, null, null, null, preEnrollmentResponse1, null, null, null, null, null, null, null);
        Map<String, String> valueMap = moduleFormatter.toStringMap(exportData);

        assertThat(valueMap.get("enrollment.referralSource"), equalTo("{ \"referringSite\": \"broadinstitute.org\"}"));
    }
}
