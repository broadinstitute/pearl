package bio.terra.pearl.core.service.workflow.merge;

import bio.terra.pearl.core.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MergeAction<T extends BaseEntity, MP> {
    private MergePair<T> pair;
    private Action action;
    private MP mergePlan; // if the action is MERGE, this is the merge plan

    public MergeAction(MergePair<T> pair, Action action) {
        this.pair = pair;
        this.action = action;
    }

    public MergeAction(MergePair<T> pair, Action action, MP mergePlan) {
        if (action ==Action.MOVE_SOURCE && pair.source() == null) {
            throw new IllegalArgumentException("source cannot be null for MOVE_SOURCE action");
        }
        if (action ==Action.DELETE_SOURCE && pair.source() == null) {
            throw new IllegalArgumentException("source cannot be null for DELETE_SOURCE action");
        }
        if (action == Action.MERGE && (mergePlan == null || pair.source() == null || pair.target() == null)) {
            throw new IllegalArgumentException("MERGE requires a source, target and plan");
        }
        this.pair = pair;
        this.action = action;
        this.mergePlan = mergePlan;
    }

    public enum Action {
        MOVE_SOURCE,
        NO_ACTION,
        MERGE,
        DELETE_SOURCE
    }
}
