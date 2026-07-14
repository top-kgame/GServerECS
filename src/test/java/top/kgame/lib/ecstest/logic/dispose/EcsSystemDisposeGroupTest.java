package top.kgame.lib.ecstest.logic.dispose;

import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.EcsStandaloneUpdateSystem;
import top.kgame.lib.ecstest.logic.util.group.SysGroupDefaultLogic;

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
