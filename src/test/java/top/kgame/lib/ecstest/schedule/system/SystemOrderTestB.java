package top.kgame.lib.ecstest.schedule.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.After;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

/**
 * 测试系统B - 在A之后执行
 */
@SystemGroup(SysGroupDefaultLogic.class)
@After(SystemOrderTestA.class)
public class SystemOrderTestB extends EcsOneComponentUpdateSystem<ComponentLexicographic> {
    @Override
    protected void update(EcsEntity entity, ComponentLexicographic component) {
        component.cache += "B";
    }
}
