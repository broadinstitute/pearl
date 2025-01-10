package bio.terra.pearl.core.service.export.formatters.module;

import bio.terra.pearl.core.model.export.ExportOptions;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.service.export.EnrolleeExportData;
import bio.terra.pearl.core.service.export.formatters.item.PropertyItemFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class PreEnrollmentResponseFormatter extends BeanModuleFormatter<PreEnrollmentResponse> {
    public static final List<String> INCLUDED_PROPERTIES = List.of(
            "referralSource"
    );

    public PreEnrollmentResponseFormatter(ExportOptions options) {
        super(options, "enrollment", "Enrollment");
    }

    @Override
    public PreEnrollmentResponse getBean(EnrolleeExportData enrolleeExportData) {
        return enrolleeExportData.getPreEnrollmentResponse();
    }

    @Override
    protected PreEnrollmentResponse newBean() {
        return new PreEnrollmentResponse();
    }

    @Override
    protected List<PropertyItemFormatter<PreEnrollmentResponse>> generateItemFormatters(ExportOptions options) {
        return INCLUDED_PROPERTIES.stream().map(propName -> new PropertyItemFormatter<>(propName, PreEnrollmentResponse.class))
                .collect(Collectors.toList());
    }
}
