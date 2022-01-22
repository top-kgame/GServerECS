package top.kgame.lib.ecs;

import top.kgame.lib.ecs.core.EcsComponent;

public class EcsEntity {
    public <T extends EcsComponent> EcsComponent getComponent(Class<T> tClass) {
        return null;
    }
}
