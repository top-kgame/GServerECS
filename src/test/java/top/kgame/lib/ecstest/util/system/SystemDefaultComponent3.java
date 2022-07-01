package top.kgame.lib.ecstest.util.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsTwoComponentUpdateSystem;
import top.kgame.lib.ecstest.util.Util;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic2;

@SystemGroup(SysGroupDefaultLogic2.class)
public class SystemDefaultComponent3 extends EcsTwoComponentUpdateSystem<Component3, ComponentLexicographic> {

    @Override
    protected void update(EcsEntity entity, Component3 component, ComponentLexicographic lexicographic) {
        Util.printSystemInfo(this.getClass(), getWorld(), entity);
        lexicographic.cache += component.data;
    }
}
