package top.kgame.lib.ecstest.performance.isolated.parallelupdate.multi;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.ParallelUpdate;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.ComponentPerfExtra;

@ParallelUpdate
public class ParallelBenchSystemB extends EcsOneComponentUpdateSystem<ComponentPerfExtra> {
    @Override
    protected void update(EcsEntity entity, ComponentPerfExtra component) {
        component.value = component.value * 37L + 11L;
        component.tick++;
    }
}
