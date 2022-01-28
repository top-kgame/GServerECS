package top.kgame.lib.ecs.core;

import top.kgame.lib.ecs.EcsEntity;

public interface EntityFactory {
    EcsEntity create(EcsEntityManager ecsEntityManager);
    int typeId();
}
