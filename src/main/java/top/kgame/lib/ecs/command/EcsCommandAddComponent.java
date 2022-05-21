package top.kgame.lib.ecs.command;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.EcsComponent;

public class EcsCommandAddComponent implements EcsCommand {
    private final EcsEntity entity;
    private final EcsComponent component;

    public EcsCommandAddComponent(EcsEntity entity, EcsComponent component) {
        this.entity = entity;
        this.component = component;
    }

    @Override
    public void execute() {
        entity.addComponent(component);
    }
}
