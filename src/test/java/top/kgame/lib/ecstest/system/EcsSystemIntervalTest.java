package top.kgame.lib.ecstest.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.After;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.annotation.TickRate;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;
import top.kgame.lib.ecstest.util.system.SystemDefaultComponent1;

@After(SystemDefaultComponent1.class)
@TickRate(EcsIntervalTest.TEST_TICK_INTERVAL)
@SystemGroup(SysGroupDefaultLogic.class)
public class EcsSystemIntervalTest extends EcsOneComponentUpdateSystem<Component1> {
    @Override
    protected void update(EcsEntity entity, Component1 component) {
        ComponentLexicographic componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        componentLexicographic.cache += "interval";
    }
}
