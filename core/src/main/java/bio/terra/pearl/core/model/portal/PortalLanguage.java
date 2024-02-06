package bio.terra.pearl.core.model.portal;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PortalLanguage extends BaseEntity {
    private String languageCode;
    private String languageName;
}
