package top.kgame.lib.ecstest.performance.isolated.tickrate;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;

public class TickEveryFrameSystem extends EcsOneComponentUpdateSystem<ComponentTick> {
    @Override
    protected void update(EcsEntity entity, ComponentTick component) {
        component.count++;
    }
}
