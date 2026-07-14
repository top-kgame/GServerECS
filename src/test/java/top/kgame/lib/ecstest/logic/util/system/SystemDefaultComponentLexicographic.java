package top.kgame.lib.ecstest.logic.util.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.logic.util.Util;
import top.kgame.lib.ecstest.logic.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.logic.util.group.SysGroupDefaultLogic2;

@SystemGroup(SysGroupDefaultLogic2.class)
public class SystemDefaultComponentLexicographic extends EcsOneComponentUpdateSystem<ComponentLexicographic> {

    @Override
    protected void update(EcsEntity entity, ComponentLexicographic  lexicographic) {
        Util.printSystemInfo(this.getClass(), getWorld(), entity);
        lexicographic.data = lexicographic.cache;
        lexicographic.cache = "";
    }
}
