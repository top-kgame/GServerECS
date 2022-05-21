package top.kgame.lib.ecs.command;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;

public class EcsCommandDestroyEntity implements EcsCommand {
    private final EcsWorld ecsWorld;
    private final EcsEntity entity;
    public EcsCommandDestroyEntity(EcsWorld ecsWorld, EcsEntity entity) {
        this.ecsWorld = ecsWorld;
        this.entity = entity;
    }

    @Override
    public void execute() {
        ecsWorld.requestDestroyEntity(entity);
    }
}
