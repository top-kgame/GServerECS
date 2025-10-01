package top.kgame.lib.ecstest.dispose;

import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsStandaloneUpdateSystem;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

@SystemGroup(SysGroupDefaultLogic.class)
public class EcsSystemDisposeGroupTest extends EcsStandaloneUpdateSystem {
    @Override
    protected void update() {
       DisposeContext disposeContext = getWorld().getContext();
       if (null ==  disposeContext) {
           return;
       }
       if (getWorld().getCurrentTime() >= disposeContext.groupSystemDisposeTime()) {
           getWorld().close();
       }
    }
}
