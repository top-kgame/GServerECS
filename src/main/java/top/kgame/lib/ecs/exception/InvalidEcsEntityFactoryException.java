package top.kgame.lib.ecs.exception;

import java.io.Serial;

public class InvalidEcsEntityFactoryException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 3167499671216858748L;

  public InvalidEcsEntityFactoryException(String message) {
        super(message);
    }
}
