package bio.terra.pearl.core.model.publishing;

import bio.terra.pearl.core.model.BaseEntity;

import java.util.List;

public record ConfigChangeList<T extends BaseEntity>(T entity, List<ConfigChange> changes) {
}
