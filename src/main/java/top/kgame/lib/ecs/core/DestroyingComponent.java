package top.kgame.lib.ecs.core;

public class DestroyingComponent implements EcsComponent {
    private DestroyingComponent(){}

    private static final DestroyingComponent INSTANCE = new DestroyingComponent();
    public static EcsComponent generate() {
        return INSTANCE;
    }
}
