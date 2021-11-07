package top.kgame.lib.ecs.exception;

public class InvalidSystemOrderAnnotation extends RuntimeException {
    public InvalidSystemOrderAnnotation(String message) {
        super(message);
    }
}
