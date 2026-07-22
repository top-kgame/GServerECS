package top.kgame.lib.ecstest.logic.isolated.parallelcustom;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.extensions.entity.BaseEntityFactory;

import java.util.Collection;
import java.util.List;

public class CustomParallelEntityFactory extends BaseEntityFactory {
    public static final int TYPE_ID = 102;

    @Override
    public int typeId() {
        return TYPE_ID;
    }

    @Override
    protected Collection<EcsComponent> generateComponent() {
        return List.of(new ComponentCustomParallelCounter());
    }
}
