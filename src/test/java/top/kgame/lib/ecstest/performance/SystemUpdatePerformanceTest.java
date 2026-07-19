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
        // 计次过少时单次 GC/调度毛刺会把平均值拉飞，提高到 80 次降低同代码复跑抖动
        runUpdateBenchmark("大量实体", 10000, 80, 500.0, false);
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

        int measurementBatches = 5;
        double[] batchAvgTimesMs = new double[measurementBatches];
        for (int batch = 0; batch < measurementBatches; batch++) {
            long startTime = System.nanoTime();
            for (int i = 0; i < iterations; i++) {
                int frame = warmup + batch * iterations + i;
                ecsWorld.update(frame * 33L);
            }
            batchAvgTimesMs[batch] = (System.nanoTime() - startTime) / 1_000_000.0 / iterations;
        }

        double avgTimeMs = EcsPerformanceTestSupport.median(batchAvgTimesMs);
        double totalTimeMs = avgTimeMs * iterations;
        double updatesPerSecond = avgTimeMs <= 0 ? Double.POSITIVE_INFINITY : 1000.0 / avgTimeMs;

        log.info("SystemUpdate 基准 [{}] ({}个实体, 预热{}次, 计时{}次): 总耗时 {} ms, 平均每次 {} ms ({}批中位数), 理论每秒更新 {}",
                scenario, entityCount, warmup, iterations, totalTimeMs, avgTimeMs,
                measurementBatches, updatesPerSecond);

        assertTrue(avgTimeMs < maxAvgMs,
                scenario + " avg time should be less than " + maxAvgMs + "ms, was " + avgTimeMs);
        if (assertUpdatesPerSecond) {
            assertTrue(updatesPerSecond > 100,
                    "Theoretical update frequency should be greater than 100 times/second");
        }
    }
}
