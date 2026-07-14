package top.kgame.lib.ecstest.performance.isolated.lifecycle;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.extensions.entity.BaseEntityFactory;

import java.util.Collection;
import java.util.List;

public class LifecycleEntityFactory extends BaseEntityFactory {
    public static final int TYPE_ID = 9201;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    protected Collection<EcsComponent> generateComponent() {
        return List.of(new ComponentLifecycle());
    }
}
