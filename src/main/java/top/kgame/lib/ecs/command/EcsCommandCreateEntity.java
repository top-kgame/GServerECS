package top.kgame.lib.ecs.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;

import java.util.function.Consumer;

public class EcsCommandCreateEntity implements EcsCommand {
    private static final Logger logger = LogManager.getLogger(EcsCommandCreateEntity.class);
    private final EcsWorld ecsWorld;
    private final int typeId;
    private final Consumer<EcsEntity> successCallback;

    public EcsCommandCreateEntity(EcsWorld ecsWorld, int typeId, Consumer<EcsEntity> successCallback) {
        this.ecsWorld = ecsWorld;
        this.typeId = typeId;
        this.successCallback = successCallback;
    }

    @Override
    public void execute() {
        EcsEntity entity = ecsWorld.createEntity(typeId);
        successCallback.accept(entity);
        logger.debug("EcsCommandCreateEntity {}", entity);
    }
}
