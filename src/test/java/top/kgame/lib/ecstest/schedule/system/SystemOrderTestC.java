package top.kgame.lib.ecstest.schedule.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.Before;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

/**
 * 测试系统C - 在B之前执行
 */
@SystemGroup(SysGroupDefaultLogic.class)
@Before(SystemOrderTestB.class)
public class SystemOrderTestC extends EcsOneComponentUpdateSystem<ComponentLexicographic> {
    @Override
    protected void update(EcsEntity entity, ComponentLexicographic component) {
        component.cache += "C";
    }
}
