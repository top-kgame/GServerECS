package top.kgame.lib.ecstest.util.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.util.Util;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic2;

@SystemGroup(SysGroupDefaultLogic2.class)
public class SystemDefaultComponentLexicographic extends EcsOneComponentUpdateSystem<ComponentLexicographic> {

    @Override
    protected void update(EcsEntity entity, ComponentLexicographic  lexicographic) {
        Util.printSystemInfo(this.getClass(), getWorld(), entity);
        lexicographic.data = lexicographic.cache;
        lexicographic.cache = "";
    }
}
