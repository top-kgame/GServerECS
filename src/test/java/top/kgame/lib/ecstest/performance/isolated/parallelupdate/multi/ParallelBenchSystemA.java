package top.kgame.lib.ecstest.performance.isolated.parallelupdate.multi;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.ParallelUpdate;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.ComponentPerfSimple;

@ParallelUpdate
public class ParallelBenchSystemA extends EcsOneComponentUpdateSystem<ComponentPerfSimple> {
    @Override
    protected void update(EcsEntity entity, ComponentPerfSimple component) {
        component.value = component.value * 31L + 17L;
        component.tick++;
    }
}
