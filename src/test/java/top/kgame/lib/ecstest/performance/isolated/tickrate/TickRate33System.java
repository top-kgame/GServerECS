package top.kgame.lib.ecstest.performance.isolated.tickrate;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.TickRate;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;

@TickRate(33)
public class TickRate33System extends EcsOneComponentUpdateSystem<ComponentTick> {
    @Override
    protected void update(EcsEntity entity, ComponentTick component) {
        component.count += 10;
    }
}
