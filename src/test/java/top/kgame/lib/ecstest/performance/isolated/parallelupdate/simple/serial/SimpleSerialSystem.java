package top.kgame.lib.ecstest.performance.isolated.parallelupdate.simple.serial;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.ComponentPerfSimple;

public class SimpleSerialSystem extends EcsOneComponentUpdateSystem<ComponentPerfSimple> {
    @Override
    protected void update(EcsEntity entity, ComponentPerfSimple component) {
        component.value = component.value * 31L + 17L;
        component.tick++;
    }
}
