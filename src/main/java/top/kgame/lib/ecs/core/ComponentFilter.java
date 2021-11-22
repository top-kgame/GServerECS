package top.kgame.lib.ecs.core;

import top.kgame.lib.ecs.exception.ComponentFilterConflict;

import java.util.BitSet;
import java.util.Objects;

/**
 * 组件类型查询条件，用于过滤实体
 */
public class ComponentFilter implements EcsCleanable {
    // 所有的都要包含
    private final BitSet subset = new BitSet(10);
    // 包含其中任意一个即可 (为空则忽略该条件)
    private final BitSet any = new BitSet(2);
    // 不能包含任何一个
    private final BitSet none = new BitSet(2);

    @Override
    public void clean() {
        subset.clear();
        any.clear();
        none.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentFilter that = (ComponentFilter) o;
        return Objects.equals(any, that.any) &&
               Objects.equals(none, that.none) &&
               Objects.equals(subset, that.subset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subset, any, none);
    }

    private void addNone(int componentIndex, Class<? extends EcsComponent> componentClass) {
        if (subset.get(componentIndex)) {
            throw new ComponentFilterConflict(
                "Component filter conflict: Cannot add " + componentClass.getName() + " to NONE list, it is already in ALL list (cannot require both must-have and must-not-have)");
        }
        if (any.get(componentIndex)) {
            throw new ComponentFilterConflict(
                "Component filter conflict: Cannot add " + componentClass.getName() + " to NONE list, it is already in ANY list (cannot require both any-of and must-not-have)");
        }
        none.set(componentIndex);
    }

    private void addSubset(int componentIndex, Class<? extends EcsComponent> componentClass) {
        if (none.get(componentIndex)) {
            throw new ComponentFilterConflict(
                "Component filter conflict: Cannot add " + componentClass.getName() + " to ALL list, it is already in NONE list (cannot require both must-have and must-not-have)");
        }
        subset.set(componentIndex);
    }

    private void addAny(int componentIndex, Class<? extends EcsComponent> componentClass) {
        if (none.get(componentIndex)) {
            throw new ComponentFilterConflict(
                "Component filter conflict: Cannot add " + componentClass.getName() + " to ANY list, it is already in NONE list (cannot require both any-of and must-not-have)");
        }
        any.set(componentIndex);
    }

    /**
     * 检查 Archetype 是否符合查询条件
     */
    public boolean isMatchingArchetype(EntityArchetype entityArchetype) {
        if (entityArchetype.contains(none)) {
            return false;
        }
        if (!entityArchetype.isSubset(subset)) {
            return false;
        }
        if (any.isEmpty()) {
            return true;
        }
        return entityArchetype.contains(any);
    }
}
