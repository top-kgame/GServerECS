package top.kgame.lib.ecs.extensions.component;

import top.kgame.lib.ecs.EcsComponent;

public class DestroyingComponent implements EcsComponent {
    private DestroyingComponent(){}

    private static final DestroyingComponent INSTANCE = new DestroyingComponent();
    public static EcsComponent generate() {
        return INSTANCE;
    }
}
