package top.kgame.lib.ecs.core;

import top.kgame.lib.ecs.EcsEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * 系统内实体并行更新执行器抽象基类。
 * <p>
 * 不负责具体分片与线程调度；子类自行实现 {@link #forEach(List, Consumer, int)}。
 * 实例通常由 {@link ParallelUpdateExecutorManager} 创建。
 */
public abstract class ParallelUpdateExecutor {
    private final ParallelUpdateExecutorManager executorManager;

    protected ParallelUpdateExecutor(ParallelUpdateExecutorManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("manager must not be null");
        }
        this.executorManager = manager;
    }

    protected final ParallelUpdateExecutorManager getExecutorManager() {
        return executorManager;
    }

    protected int resolveTaskCount(int entityCount, int minEntitiesPerBatch) {
        int byGrain = Math.max(1, entityCount / minEntitiesPerBatch);
        return Math.min(getExecutorManager().getParallelism(), byGrain);
    }

    /** 池线程处理前 {@code taskCount - 1} 个任务，调用线程处理最后一个；*/
    protected void runParallel(int taskCount, IntConsumer task) {
        final AtomicInteger remaining = new AtomicInteger(taskCount - 1);
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final ExecutorService executor = getExecutorManager().getExecutor();
        for (int taskIndex = 0; taskIndex < taskCount - 1; taskIndex++) {
            final int workerIndex = taskIndex;
            executor.execute(() -> {
                try {
                    task.accept(workerIndex);
                } catch (Throwable t) {
                    error.compareAndSet(null, t);
                } finally {
                    remaining.decrementAndGet();
                }
            });
        }
        //避免当前线程空转
        try {
            task.accept(taskCount - 1);
        } catch (Throwable t) {
            error.compareAndSet(null, t);
        }

        while (remaining.get() > 0) {
            Thread.onSpinWait();
        }
        Throwable throwable = error.get();
        if (throwable != null) {
            throw new RuntimeException(getClass().getSimpleName() + " task failed", throwable);
        }
    }

    /**
     * 由子类实现：实体分片与线程调度均可自定义。
     */
    public abstract void forEach(List<EcsEntity> entities,
                                    Consumer<EcsEntity> action,
                                    int minEntitiesPerBatch);
}
