package top.kgame.lib.ecstest.util.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.After;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsTwoComponentUpdateSystem;
import top.kgame.lib.ecstest.util.Util;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

@SystemGroup(SysGroupDefaultLogic.class)
@After(value = SystemDefaultComponent1.class)
public class SystemDefaultComponent2 extends EcsTwoComponentUpdateSystem<Component2, ComponentLexicographic> {

    @Override
    protected void update(EcsEntity entity, Component2 component, ComponentLexicographic  lexicographic) {
        Util.printSystemInfo(this.getClass(), getWorld(), entity);
        lexicographic.cache += component.data;
    }
}
