package top.kgame.lib.ecstest.logic.dispose;

import top.kgame.lib.ecs.EcsStandaloneUpdateSystem;

public class EcsSystemDisposeTopTest extends EcsStandaloneUpdateSystem {
    @Override
    protected void update() {
        DisposeContext disposeContext = getWorld().getContext();
        if (null ==  disposeContext) {
            return;
        }
        if (getWorld().getCurrentTime() >= disposeContext.topSystemDisposeTime()) {
            getWorld().close();
        }
    }
}
