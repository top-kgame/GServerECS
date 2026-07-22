package top.kgame.lib.ecstest.logic.isolated.parallelstride;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.ParallelUpdate;
import top.kgame.lib.ecs.extensions.parallel.StrideParallelUpdateExecutor;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;

@ParallelUpdate(executor = StrideParallelUpdateExecutor.class)
public class StrideIncrementSystem extends EcsOneComponentUpdateSystem<ComponentStrideCounter> {
    @Override
    protected void update(EcsEntity entity, ComponentStrideCounter component) {
        component.count++;
    }
}
