package top.kgame.lib.ecs.command;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;

/**
 * 注意：在EcsCommandScope.WORLD作用域下执行此命令会直接移除entity，不会触发对应的EcsDestroySystem逻辑
 */
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
