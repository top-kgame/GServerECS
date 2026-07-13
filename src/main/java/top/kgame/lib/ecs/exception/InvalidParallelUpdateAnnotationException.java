package top.kgame.lib.ecs.exception;

import java.io.Serial;

public class InvalidParallelUpdateAnnotationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidParallelUpdateAnnotationException(Class<?> clazz) {
        super("@ParallelUpdate can only be used on EcsLogicSystem: " + clazz.getName());
    }
}
