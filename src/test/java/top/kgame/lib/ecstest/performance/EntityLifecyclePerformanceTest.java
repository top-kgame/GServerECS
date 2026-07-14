package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecstest.performance.isolated.lifecycle.LifecycleEntityFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 实体生命周期性能基准：create / destroy / churn。
 */
public class EntityLifecyclePerformanceTest {
    private static final Logger log = LogManager.getLogger(EntityLifecyclePerformanceTest.class);
    private static final String LIFECYCLE_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.lifecycle";

    private EcsWorld ecsWorld;
    private long timeCursor;

    @BeforeEach
    void setUp() {
        ecsWorld = EcsWorld.generateInstance(LIFECYCLE_PACKAGE);
        timeCursor = 0L;
    }

    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    @Test
    void testCreateEntityThroughputMedium() {
        runCreateBenchmark(2000, 50.0);
    }

    @Test
    void testCreateEntityThroughputLarge() {
        runCreateBenchmark(10000, 200.0);
    }

    @Test
    void testDestroyEntityThroughputMedium() {
        runDestroyBenchmark(2000, 80.0);
    }

    @Test
    void testDestroyEntityThroughputLarge() {
        runDestroyBenchmark(10000, 300.0);
    }

    @Test
    void testCreateDestroyChurnThroughput() {
        int batchSize = 1000;
        int rounds = 40;
        warmupLifecycle(batchSize);

        long startTime = System.nanoTime();
        for (int r = 0; r < rounds; r++) {
            EcsEntity[] entities = new EcsEntity[batchSize];
            for (int i = 0; i < batchSize; i++) {
                entities[i] = ecsWorld.createEntity(LifecycleEntityFactory.class);
            }
            for (EcsEntity entity : entities) {
                ecsWorld.requestDestroyEntity(entity);
            }
            tick();
        }
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgRoundMs = totalTimeMs / rounds;
        double opsPerSecond = batchSize * 2.0 * rounds * 1000.0 / totalTimeMs;

        log.info("实体 churn 基准 (每轮{} create+destroy, {}轮): 总耗时 {} ms, 平均每轮 {} ms, 吞吐约 {} 次生命周期op/秒",
                batchSize, rounds, totalTimeMs, avgRoundMs, opsPerSecond);

        assertTrue(avgRoundMs < 100.0,
                "Churn round avg should be < 100ms, was " + avgRoundMs);
    }

    private void runCreateBenchmark(int entityCount, double maxTotalMs) {
        warmupLifecycle(Math.min(entityCount, 1000));

        long startTime = System.nanoTime();
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(LifecycleEntityFactory.class);
        }
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgCreateUs = totalTimeMs * 1000.0 / entityCount;
        double createsPerSecond = entityCount * 1000.0 / totalTimeMs;

        log.info("实体 create 基准 ({}个): 总耗时 {} ms, 平均每次 {} us, 吞吐约 {} 实体/秒",
                entityCount, totalTimeMs, avgCreateUs, createsPerSecond);

        assertTrue(totalTimeMs < maxTotalMs,
                "Create total should be < " + maxTotalMs + "ms, was " + totalTimeMs);
    }

    private void runDestroyBenchmark(int entityCount, double maxTotalMs) {
        warmupLifecycle(Math.min(entityCount, 1000));

        EcsEntity[] entities = new EcsEntity[entityCount];
        for (int i = 0; i < entityCount; i++) {
            entities[i] = ecsWorld.createEntity(LifecycleEntityFactory.class);
        }

        long startTime = System.nanoTime();
        for (EcsEntity entity : entities) {
            ecsWorld.requestDestroyEntity(entity);
        }
        tick();
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgDestroyUs = totalTimeMs * 1000.0 / entityCount;
        double destroysPerSecond = entityCount * 1000.0 / totalTimeMs;

        log.info("实体 destroy 基准 ({}个,含update消化): 总耗时 {} ms, 平均每次 {} us, 吞吐约 {} 实体/秒",
                entityCount, totalTimeMs, avgDestroyUs, destroysPerSecond);

        assertTrue(totalTimeMs < maxTotalMs,
                "Destroy total should be < " + maxTotalMs + "ms, was " + totalTimeMs);
    }

    private void warmupLifecycle(int batchSize) {
        int warmupRounds = EcsPerformanceTestSupport.resolveWarmupIterations(batchSize);
        // 生命周期路径以 create/destroy 为主，预热轮数不必拉到世界 update 级别的峰值
        warmupRounds = Math.min(warmupRounds, 500);
        EcsPerformanceTestSupport.warmupCallable(warmupRounds, () -> {
            EcsEntity[] entities = new EcsEntity[batchSize];
            for (int i = 0; i < batchSize; i++) {
                entities[i] = ecsWorld.createEntity(LifecycleEntityFactory.class);
            }
            for (EcsEntity entity : entities) {
                ecsWorld.requestDestroyEntity(entity);
            }
            tick();
        });
    }

    private void tick() {
        timeCursor += 33L;
        ecsWorld.update(timeCursor);
    }
}
