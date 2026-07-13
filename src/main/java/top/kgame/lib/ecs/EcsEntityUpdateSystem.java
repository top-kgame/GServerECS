package top.kgame.lib.ecs;

import top.kgame.lib.ecs.annotation.ParallelUpdate;
import top.kgame.lib.ecs.core.ComponentFilter;
import top.kgame.lib.ecs.core.ComponentFilterParam;
import top.kgame.lib.ecs.core.EntityQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class EcsEntityUpdateSystem extends EcsSystem {
    private final List<ComponentFilterParam<?>> extraMatchComponent = new ArrayList<>();
    private boolean parallelUpdate = false;
    private EntityQuery entityQuery;

    @Override
    protected void onInit() {
        ParallelUpdate parallelUpdateAnno = this.getClass().getAnnotation(ParallelUpdate.class);
        if (parallelUpdateAnno != null) {
            parallelUpdate = true;
        }

        processExtraComponent();
        List<ComponentFilterParam<?>> componentFilterParams = new ArrayList<>();
        componentFilterParams.addAll(getMatchComponent());
        componentFilterParams.addAll(extraMatchComponent);
        registerEntityFilter(ComponentFilter.generate(super.getWorld(), componentFilterParams));

    }

    @Override
    protected boolean needUpdate() {
        if (entityQuery == null) {
            return false;
        }
        return !entityQuery.isEmpty();
    }

    @Override
    final protected void update() {
        Consumer<EcsEntity> action = createUpdateAction();
        List<EcsEntity> entities = getAllMatchEntity();
        if (parallelUpdate) {
            ecsSystemManager.getParallelUpdateExecutor().forEach(entities, action);
        } else {
            for (EcsEntity entity : entities) {
                action.accept(entity);
            }
        }
    }

    protected void registerEntityFilter(ComponentFilter componentTypes) {
        if (entityQuery == null) {
            entityQuery = getWorld().findOrCreateEntityQuery(componentTypes);
            return;
        }
        if (!entityQuery.matchFilter(componentTypes)) {
            throw new UnsupportedOperationException("Repeatedly setting EntityQuery");
        }
    }

    protected List<EcsEntity> getAllMatchEntity() {
        if (entityQuery == null) {
            return Collections.emptyList();
        }
        return entityQuery.getEntityList();
    }

    protected boolean isParallelUpdate() {
        return parallelUpdate;
    }

    protected Consumer<EcsEntity> createUpdateAction() {
        throw new UnsupportedOperationException(
                "createUpdateAction() must be implemented, or update() must be overridden");
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
