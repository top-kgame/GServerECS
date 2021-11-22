package top.kgame.lib.ecs.core;


import java.util.Objects;

public class ComponentFilterParam<T extends EcsComponent> {
    private final ComponentFilterMode filterModeType;
    private Class<T> type;

    private ComponentFilterParam(ComponentFilterMode filterModeType) {
        this.filterModeType = filterModeType;
    }

    /**
     * 必须包含
     */
    public static <T extends EcsComponent> ComponentFilterParam<T> require(Class<T> type) {
        ComponentFilterParam<T> componentMatchType = new ComponentFilterParam<>(ComponentFilterMode.Subset);
        componentMatchType.type = type;
        return componentMatchType;
    }

    /**
     * 包含其中任意一个即可
     */
    public static <T extends EcsComponent> ComponentFilterParam<T> anyOf(Class<T> type) {
        ComponentFilterParam<T> componentMatchType = new ComponentFilterParam<>(ComponentFilterMode.ANY);
        componentMatchType.type = type;
        return componentMatchType;
    }
    
    /**
     * 不包含
     */
    public static <T extends EcsComponent> ComponentFilterParam<T> exclude(Class<T> type) {
        ComponentFilterParam<T> componentMatchType = new ComponentFilterParam<>(ComponentFilterMode.NONE);
        componentMatchType.type = type;
        return componentMatchType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentFilterParam<?> that = (ComponentFilterParam<?>) o;
        return Objects.equals(type, that.type) && filterModeType == that.filterModeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, filterModeType);
    }

    public Class<T> getType() {
        return type;
    }

    public ComponentFilterMode getFilterModeType() {
        return filterModeType;
    }
}