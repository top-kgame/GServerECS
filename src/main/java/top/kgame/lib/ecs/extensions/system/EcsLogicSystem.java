package top.kgame.lib.ecs.extensions.system;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.core.ComponentFilter;
import top.kgame.lib.ecs.core.ComponentFilterParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract class EcsLogicSystem extends EcsSystem {
    private final List<ComponentFilterParam<?>> extraMatchComponent = new ArrayList<>();

    @Override
    protected void onInit() {
        processExtraComponent();
        List<ComponentFilterParam<?>> componentFilterParams = new ArrayList<>();
        componentFilterParams.addAll(getMatchComponent());
        componentFilterParams.addAll(extraMatchComponent);
        registerEntityFilter(ComponentFilter.generate(super.getWorld(), componentFilterParams));
    }

    public List<ComponentFilterParam<?>> getExtraMatchComponent() {
        return extraMatchComponent;
    }

    private void processExtraComponent() {
        Collection<Class<? extends EcsComponent>> requireComponent = getExtraRequirementComponent();
        if (requireComponent != null && !requireComponent.isEmpty()) {
            for (Class<? extends EcsComponent> clazz : requireComponent) {
                extraMatchComponent.add(ComponentFilterParam.require(clazz));
            }
        }
        Collection<Class<? extends EcsComponent>> excludeComponent = getExtraExcludeComponent();
        if (excludeComponent != null && !excludeComponent.isEmpty()) {
            for (Class<? extends EcsComponent> clazz : excludeComponent) {
                extraMatchComponent.add(ComponentFilterParam.exclude(clazz));
            }
        }
    }

    protected abstract Collection<ComponentFilterParam<?>> getMatchComponent();

    /**
     * 额外需要关注的Component类
     */
    public abstract Collection<Class<? extends EcsComponent>> getExtraRequirementComponent();

    /**
     * 额外需要排除的Component类
     */
    public abstract Collection<Class<? extends EcsComponent>> getExtraExcludeComponent();
}
