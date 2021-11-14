package top.kgame.lib.ecs.exception;

public class NoDefaultConstructorException extends RuntimeException {
    public NoDefaultConstructorException(Class<?> type) {
        super("createEntity failed! component "
                + type.getName() + " don't has default constructor");
    }
}
