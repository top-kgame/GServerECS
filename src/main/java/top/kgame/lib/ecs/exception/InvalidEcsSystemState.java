package top.kgame.lib.ecs.exception;

public class InvalidEcsSystemState extends RuntimeException {
    public InvalidEcsSystemState(String message) {
        super(message);
    }
}
