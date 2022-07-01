package top.kgame.lib.ecstest.util.system;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.command.EcsCommand;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.util.KV;
import top.kgame.lib.ecstest.util.Util;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

@SystemGroup(SysGroupDefaultLogic.class)
public class SystemDelayAddComponentCommandHolder extends EcsOneComponentUpdateSystem<ComponentCommandHolder> {

    @Override
    protected void update(EcsEntity entity, ComponentCommandHolder component) {
        Util.printSystemInfo(this.getClass(), getWorld(), entity);
        for (KV<EcsCommandScope, EcsCommand> kv : component.getCommands()) {
            addDelayCommand(kv.value(), kv.key());
        }
        component.clear();
    }
}

