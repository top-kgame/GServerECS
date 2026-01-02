package top.kgame.lib.ecstest.schedule.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

/**
 * 测试系统E - 无注解，用于测试默认字典序（应该在D之后）
 */
@SystemGroup(SysGroupDefaultLogic.class)
public class SystemOrderTestE extends EcsOneComponentUpdateSystem<ComponentLexicographic> {
    @Override
    protected void update(EcsEntity entity, ComponentLexicographic component) {
        component.cache += "E";
    }
}
