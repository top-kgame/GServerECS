package top.kgame.lib.ecstest.logic.isolated.parallelcustom;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.ParallelUpdate;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;

@ParallelUpdate(executor = IndexHashParallelUpdateExecutor.class)
public class CustomParallelIncrementSystem
        extends EcsOneComponentUpdateSystem<ComponentCustomParallelCounter> {
    @Override
    protected void update(EcsEntity entity, ComponentCustomParallelCounter component) {
        component.count++;
    }
}
