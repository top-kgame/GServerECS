package top.kgame.lib.ecstest.performance.isolated.parallelupdate.twocomponent;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.ParallelUpdate;
import top.kgame.lib.ecs.extensions.system.EcsTwoComponentUpdateSystem;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.ComponentPerfExtra;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.ComponentPerfSimple;

@ParallelUpdate
public class ParallelTwoComponentBenchSystem
        extends EcsTwoComponentUpdateSystem<ComponentPerfSimple, ComponentPerfExtra> {
    @Override
    protected void update(EcsEntity entity, ComponentPerfSimple simple, ComponentPerfExtra extra) {
        simple.value = simple.value * 31L + 17L;
        simple.tick++;
        extra.value = extra.value * 37L + 11L;
        extra.tick++;
    }
}
