package top.kgame.lib.ecstest.logic.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.ParallelUpdateExecutor;
import top.kgame.lib.ecs.core.ParallelUpdateExecutorManager;
import top.kgame.lib.ecs.extensions.parallel.RangeParallelUpdateExecutor;
import top.kgame.lib.ecs.extensions.parallel.StrideParallelUpdateExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelUpdateExecutorTest {
    private static final int ENTITY_COUNT = 512;
    private static final int PARALLELISM = 4;
    private static final int GRAIN = ParallelUpdateExecutorManager.DEFAULT_MIN_ENTITIES_PER_BATCH;

    private ParallelUpdateExecutorManager manager;
    private List<EcsEntity> entities;

    @BeforeEach
    void setUp() {
        manager = new ParallelUpdateExecutorManager(PARALLELISM);
        entities = new ArrayList<>(ENTITY_COUNT);
        for (int i = 0; i < ENTITY_COUNT; i++) {
            entities.add(new EcsEntity(null, i, 0));
        }
    }

    @AfterEach
    void tearDown() {
        manager.clean();
    }

    @Test
    void managerReturnsDistinctAlgorithmInstances() {
        ParallelUpdateExecutor range = manager.getInstance(RangeParallelUpdateExecutor.class);
        ParallelUpdateExecutor stride = manager.getInstance(StrideParallelUpdateExecutor.class);
        assertSame(range, manager.getInstance(RangeParallelUpdateExecutor.class));
        assertSame(stride, manager.getInstance(StrideParallelUpdateExecutor.class));
        assertNotSame(range, stride);
        assertInstanceOf(RangeParallelUpdateExecutor.class, range);
        assertInstanceOf(StrideParallelUpdateExecutor.class, stride);
    }

    @Test
    void rangeProcessesEveryEntityOnce() {
        assertProcessesEveryEntityOnce(RangeParallelUpdateExecutor.class);
    }

    @Test
    void strideProcessesEveryEntityOnce() {
        assertProcessesEveryEntityOnce(StrideParallelUpdateExecutor.class);
    }

    @Test
    void customExecutorProcessesEveryEntityOnce() {
        AtomicIntegerArray visitCount = new AtomicIntegerArray(ENTITY_COUNT);
        manager.getInstance(HashParallelUpdateExecutor.class)
                .forEach(entities, entity -> visitCount.incrementAndGet(entity.getIndex()), GRAIN);

        for (int i = 0; i < ENTITY_COUNT; i++) {
            assertEquals(1, visitCount.get(i), "custom executor must visit index " + i + " exactly once");
        }
        assertSame(manager.getInstance(HashParallelUpdateExecutor.class),
                manager.getInstance(HashParallelUpdateExecutor.class));
    }

    @Test
    void registerCustomExecutorInstance() {
        HashParallelUpdateExecutor executor = new HashParallelUpdateExecutor(manager);
        manager.register(HashParallelUpdateExecutor.class, executor);
        assertSame(executor, manager.getInstance(HashParallelUpdateExecutor.class));
    }

    @Test
    void rangeUsesContiguousSlices() {
        ParallelUpdateExecutorManager smallManager = new ParallelUpdateExecutorManager(2);
        try {
            AtomicIntegerArray visitCount = new AtomicIntegerArray(ENTITY_COUNT);
            smallManager.getInstance(RangeParallelUpdateExecutor.class)
                    .forEach(entities, entity -> visitCount.incrementAndGet(entity.getIndex()), GRAIN);

            for (int i = 0; i < ENTITY_COUNT; i++) {
                assertEquals(1, visitCount.get(i), "index " + i + " should be visited once");
            }
            assertEquals(0, RangeParallelUpdateExecutor.batchStart(ENTITY_COUNT, 2, 0));
            assertEquals(256, RangeParallelUpdateExecutor.batchStart(ENTITY_COUNT, 2, 1));
            assertEquals(512, RangeParallelUpdateExecutor.batchStart(ENTITY_COUNT, 2, 2));
        } finally {
            smallManager.clean();
        }
    }

    @Test
    void customMinEntitiesPerBatchControlsTaskCount() {
        List<EcsEntity> smallList = entities.subList(0, 200);
        AtomicIntegerArray visitCount = new AtomicIntegerArray(200);

        manager.getInstance(RangeParallelUpdateExecutor.class)
                .forEach(smallList, entity -> visitCount.incrementAndGet(entity.getIndex()), 50);

        for (int i = 0; i < 200; i++) {
            assertEquals(1, visitCount.get(i), "index " + i + " should be visited once");
        }
    }

    @Test
    void strideUsesInterleavedIndices() {
        AtomicIntegerArray visitCount = new AtomicIntegerArray(ENTITY_COUNT);
        manager.getInstance(StrideParallelUpdateExecutor.class)
                .forEach(entities, entity -> visitCount.incrementAndGet(entity.getIndex()), GRAIN);

        for (int i = 0; i < ENTITY_COUNT; i++) {
            assertEquals(1, visitCount.get(i), "stride must visit index " + i + " exactly once");
        }

        int taskCount = Math.min(PARALLELISM, ENTITY_COUNT / GRAIN);
        assertTrue(taskCount >= 2, "expected parallel stride tasks");
        for (int t = 0; t < taskCount; t++) {
            for (int i = t; i < ENTITY_COUNT; i += taskCount) {
                assertEquals(t, i % taskCount);
            }
        }
    }

    private void assertProcessesEveryEntityOnce(Class<? extends ParallelUpdateExecutor> executorClass) {
        AtomicIntegerArray visitCount = new AtomicIntegerArray(ENTITY_COUNT);
        manager.getInstance(executorClass)
                .forEach(entities, entity -> visitCount.incrementAndGet(entity.getIndex()), GRAIN);

        for (int i = 0; i < ENTITY_COUNT; i++) {
            assertEquals(1, visitCount.get(i),
                    executorClass.getSimpleName() + " must visit index " + i + " exactly once");
        }
    }

    /** 按 entity index 取模分片的自定义执行器示例。 */
    public static final class HashParallelUpdateExecutor extends ParallelUpdateExecutor {
        public HashParallelUpdateExecutor(ParallelUpdateExecutorManager manager) {
            super(manager);
        }

        @Override
        public void forEach(List<EcsEntity> entities,
                            Consumer<EcsEntity> action,
                            int minEntitiesPerBatch) {
            final int size = entities.size();
            if (size == 0) {
                return;
            }
            final int taskCount = resolveTaskCount(size, minEntitiesPerBatch);
            if (taskCount == 1) {
                entities.forEach(action);
                return;
            }
            runParallel(taskCount, taskIndex -> {
                for (int i = 0; i < size; i++) {
                    if (Math.floorMod(entities.get(i).getIndex(), taskCount) == taskIndex) {
                        action.accept(entities.get(i));
                    }
                }
            });
        }
    }
}
