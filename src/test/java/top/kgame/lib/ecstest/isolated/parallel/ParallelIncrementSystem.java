package top.kgame.lib.ecstest.isolated.parallel;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.ParallelUpdate;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;

@ParallelUpdate
public class ParallelIncrementSystem extends EcsOneComponentUpdateSystem<ComponentParallelCounter> {
    @Override
    protected void update(EcsEntity entity, ComponentParallelCounter component) {
        component.count++;
    }
}
