package top.kgame.lib.ecs;

import top.kgame.lib.ecs.core.EcsCleanable;
import top.kgame.lib.ecs.core.EcsSystemManager;

public abstract class EcsSystem implements EcsCleanable {
    @Override
    public void clean() {

    }

    public abstract void onInit();

    public abstract void update();

    protected abstract void onDestroy();

    public void tryUpdate() {
        update();
    }

    public void init(EcsSystemManager ecsSystemManager) {
        onInit();
    }
}
