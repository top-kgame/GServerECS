package top.kgame.lib.ecs.command;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.EcsComponent;

public class EcsCommandRemoveComponent implements EcsCommand {
    private final EcsEntity entity;
    private final Class<? extends EcsComponent> componentCls;

    public EcsCommandRemoveComponent(EcsEntity entity, Class<? extends EcsComponent> componentCls) {
        this.entity = entity;
        this.componentCls = componentCls;
    }

    @Override
    public void execute() {
        entity.removeComponent(componentCls);
    }
}
