package top.kgame.lib.ecstest.isolated.parallel;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.extensions.entity.BaseEntityFactory;

import java.util.Collection;
import java.util.List;

public class ParallelEntityFactory extends BaseEntityFactory {
    public static final int TYPE_ID = 100;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    protected Collection<EcsComponent> generateComponent() {
        return List.of(new ComponentParallelCounter());
    }
}
