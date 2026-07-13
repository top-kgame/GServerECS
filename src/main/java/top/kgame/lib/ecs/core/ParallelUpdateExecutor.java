package top.kgame.lib.ecs.core;

import top.kgame.lib.ecs.EcsEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ParallelUpdateExecutor implements EcsCleanable {
    private final ExecutorService executor;

    public ParallelUpdateExecutor() {
        this(Math.max(Runtime.getRuntime().availableProcessors(), 2));
    }

    public ParallelUpdateExecutor(int parallelism) {
        if (parallelism <= 1) {
            throw new IllegalArgumentException("ParallelUpdateExecutor parallelism must be greater than 1");
        }
        AtomicInteger threadIndex = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(parallelism, r -> {
            Thread thread = new Thread(r, "ecs-parallel-update-" + threadIndex.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
    }

    public void forEach(List<EcsEntity> entities, Consumer<EcsEntity> action) {
        if (entities.isEmpty()) {
            return;
        }

        if (entities.size() == 1) {
            action.accept(entities.getFirst());
            return;
        }

        CountDownLatch latch = new CountDownLatch(entities.size());
        AtomicReference<Throwable> error = new AtomicReference<>();
        for (EcsEntity entity : entities) {
            executor.execute(() -> {
                try {
                    action.accept(entity);
                } catch (Throwable t) {
                    error.compareAndSet(null, t);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("ParallelUpdateExecutor interrupted while waiting for tasks", e);
        }
        Throwable throwable = error.get();
        if (throwable != null) {
            throw new RuntimeException("ParallelUpdateExecutor task failed", throwable);
        }
    }

    @Override
    public void clean() {
        executor.shutdown();
    }
}
