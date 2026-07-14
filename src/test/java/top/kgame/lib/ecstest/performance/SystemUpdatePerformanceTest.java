package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecstest.logic.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SystemUpdate 性能测试：稳态实体下 {@link EcsWorld#update} 吞吐回归门槛。
 * <p>
 * 正式计时前通过 {@link EcsPerformanceTestSupport} 做充分预热。
 */
public class SystemUpdatePerformanceTest {
    private static final Logger log = LogManager.getLogger(SystemUpdatePerformanceTest.class);
    private EcsWorld ecsWorld;

    @BeforeEach
    void setUp() {
        ecsWorld = EcsWorld.generateInstance("top.kgame.lib.ecstest.logic.util");
    }

    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    @Test
    void testSystemUpdatePerformanceWithSmallEntityCount() {
        runUpdateBenchmark("少量实体", 100, 1000, 10.0, false);
    }

    @Test
    void testSystemUpdatePerformanceWithMediumEntityCount() {
        runUpdateBenchmark("中等实体", 1000, 100, 50.0, false);
    }

    @Test
    void testSystemUpdatePerformanceWithLargeEntityCount() {
        runUpdateBenchmark("大量实体", 10000, 10, 500.0, false);
    }

    @Test
    void testMultipleSystemUpdatePerformance() {
        int entityCount = 500;
        int iterations = 100;
        for (int i = 0; i < entityCount; i++) {
            if (i % 3 == 0) {
                ecsWorld.createEntity(EntityIndex.E1.getId());
            } else if (i % 3 == 1) {
                ecsWorld.createEntity(EntityIndex.E12.getId());
            } else {
                ecsWorld.createEntity(EntityIndex.E123.getId());
            }
        }
        measureAndAssert("多类型实体/多系统", entityCount, iterations, 30.0, false);
    }

    @Test
    void testSystemUpdateFrequencyPerformance() {
        runUpdateBenchmark("更新频率", 1000, 1000, 5.0, true);
    }

    private void runUpdateBenchmark(String scenario,
                                    int entityCount,
                                    int iterations,
                                    double maxAvgMs,
                                    boolean assertUpdatesPerSecond) {
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        measureAndAssert(scenario, entityCount, iterations, maxAvgMs, assertUpdatesPerSecond);
    }

    private void measureAndAssert(String scenario,
                                  int entityCount,
                                  int iterations,
                                  double maxAvgMs,
                                  boolean assertUpdatesPerSecond) {
        int warmup = EcsPerformanceTestSupport.warmupWorldForEntities(ecsWorld, entityCount);

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ecsWorld.update((i + warmup) * 33L);
        }
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        double updatesPerSecond = avgTimeMs <= 0 ? Double.POSITIVE_INFINITY : 1000.0 / avgTimeMs;

        log.info("SystemUpdate 基准 [{}] ({}个实体, 预热{}次, 计时{}次): 总耗时 {} ms, 平均每次 {} ms, 理论每秒更新 {}",
                scenario, entityCount, warmup, iterations, totalTimeMs, avgTimeMs, updatesPerSecond);

        assertTrue(avgTimeMs < maxAvgMs,
                scenario + " avg time should be less than " + maxAvgMs + "ms, was " + avgTimeMs);
        if (assertUpdatesPerSecond) {
            assertTrue(updatesPerSecond > 100,
                    "Theoretical update frequency should be greater than 100 times/second");
        }
    }
}
