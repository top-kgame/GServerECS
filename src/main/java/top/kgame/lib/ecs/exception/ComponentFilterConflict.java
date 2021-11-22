package top.kgame.lib.ecs.exception;

public class ComponentFilterConflict extends RuntimeException {
    public ComponentFilterConflict(String message) {
        super(message);
    }
}
