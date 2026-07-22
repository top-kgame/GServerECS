package top.kgame.lib.ecs.core;


import top.kgame.lib.ecs.exception.ParallelUpdateExecutorException;
import top.kgame.lib.ecs.extensions.parallel.RangeParallelUpdateExecutor;
import top.kgame.lib.ecs.extensions.parallel.StrideParallelUpdateExecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ParallelUpdateExecutor} 管理器：按执行器 Class 创建（并缓存）实例。
 * <p>
 * 同一管理器下的执行器共享线程池。
 */
public final class ParallelUpdateExecutorManager implements EcsCleanable {
    /** 未在注解中指定时使用的默认最小批次实体数。 */
    public static final int DEFAULT_MIN_ENTITIES_PER_BATCH = 256;

    private final ExecutorService executor;
    private final int parallelism;
    private final Map<Class<? extends ParallelUpdateExecutor>, ParallelUpdateExecutor> executors =
            new ConcurrentHashMap<>();

    public ParallelUpdateExecutorManager() {
        this(Math.max(Runtime.getRuntime().availableProcessors(), 2));
    }

    public ParallelUpdateExecutorManager(int parallelism) {
        if (parallelism <= 1) {
            throw new IllegalArgumentException("ParallelUpdateExecutorManager parallelism must be greater than 1");
        }
        AtomicInteger threadIndex = new AtomicInteger();
        this.parallelism = parallelism;
        this.executor = Executors.newFixedThreadPool(parallelism, r -> {
            Thread thread = new Thread(r, "ecs-parallel-update-" + threadIndex.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });
        register(RangeParallelUpdateExecutor.class, new RangeParallelUpdateExecutor(this));
        register(StrideParallelUpdateExecutor.class, new StrideParallelUpdateExecutor(this));
    }

    /**
     * 按执行器类型获取实例。
     * <p>
     * {@link ParallelUpdateExecutor} 本身作为注解占位时会回退为 {@link RangeParallelUpdateExecutor}。
     * 未注册的类型将尝试 {@code (ParallelUpdateExecutorManager)} 构造并缓存。
     */
    public ParallelUpdateExecutor getInstance(Class<? extends ParallelUpdateExecutor> executorClass) {
        Class<? extends ParallelUpdateExecutor> resolved = resolveExecutorClass(executorClass);
        return executors.computeIfAbsent(resolved, this::instantiate);
    }

    /**
     * 注册自定义执行器实例（可注入依赖时使用）。
     */
    public void register(Class<? extends ParallelUpdateExecutor> executorClass,
                         ParallelUpdateExecutor executor) {
        if (executorClass == null || executor == null) {
            throw new IllegalArgumentException("executorClass and executor must not be null");
        }
        if (executorClass == ParallelUpdateExecutor.class) {
            throw new IllegalArgumentException("Cannot register ParallelUpdateExecutor abstract type");
        }
        executors.put(executorClass, executor);
    }

    static Class<? extends ParallelUpdateExecutor> resolveExecutorClass(
            Class<? extends ParallelUpdateExecutor> executorClass) {
        if (executorClass == null || executorClass == ParallelUpdateExecutor.class) {
            return RangeParallelUpdateExecutor.class;
        }
        return executorClass;
    }

    private ParallelUpdateExecutor instantiate(Class<? extends ParallelUpdateExecutor> executorClass) {
        try {
            Constructor<? extends ParallelUpdateExecutor> constructor =
                    executorClass.getConstructor(ParallelUpdateExecutorManager.class);
            return constructor.newInstance(this);
        } catch (NoSuchMethodException e) {
            throw new ParallelUpdateExecutorException(
                    "ParallelUpdateExecutor must have a public constructor(ParallelUpdateExecutorManager), "
                            + "or be registered via manager.register(): " + executorClass.getName(), e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ParallelUpdateExecutorException(
                    "Failed to instantiate ParallelUpdateExecutor: " + executorClass.getName(), e);
        }
    }

    @Override
    public void clean() {
        executor.shutdown();
        executors.clear();
    }

    public int getParallelism() {
        return parallelism;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
