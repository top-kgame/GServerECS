package top.kgame.lib.ecstest.logic.util.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsTwoComponentUpdateSystem;
import top.kgame.lib.ecstest.logic.util.Util;
import top.kgame.lib.ecstest.logic.util.component.Component4;
import top.kgame.lib.ecstest.logic.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.logic.util.group.SysGroupDefaultLogic2;

@SystemGroup(SysGroupDefaultLogic2.class)
public class SystemDefaultComponent4 extends EcsTwoComponentUpdateSystem<Component4, ComponentLexicographic> {

    @Override
    protected void update(EcsEntity entity, Component4 component, ComponentLexicographic  lexicographic) {
        Util.printSystemInfo(this.getClass(), getWorld(), entity);
        lexicographic.cache += component.data;
    }
}
