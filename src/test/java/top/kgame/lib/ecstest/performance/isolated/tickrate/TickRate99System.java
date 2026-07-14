package top.kgame.lib.ecstest.performance.isolated.tickrate;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.TickRate;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;

@TickRate(99)
public class TickRate99System extends EcsOneComponentUpdateSystem<ComponentTick> {
    @Override
    protected void update(EcsEntity entity, ComponentTick component) {
        component.count += 100;
    }
}
