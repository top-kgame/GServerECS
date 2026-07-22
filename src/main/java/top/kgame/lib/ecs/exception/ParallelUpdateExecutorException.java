package top.kgame.lib.ecs.exception;

/**
 * 并行更新执行器创建或注册失败。
 */
public class ParallelUpdateExecutorException extends RuntimeException {
    public ParallelUpdateExecutorException(String message, Throwable cause) {
        super(message, cause);
    }
}
