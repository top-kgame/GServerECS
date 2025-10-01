package top.kgame.lib.ecstest.dispose;

import top.kgame.lib.ecs.extensions.system.EcsStandaloneUpdateSystem;


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
