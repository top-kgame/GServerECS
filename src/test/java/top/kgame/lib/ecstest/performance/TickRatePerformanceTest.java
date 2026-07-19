package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecstest.performance.isolated.tickrate.TickEntityFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 混合 {@code @TickRate} 系统下的 world.update 性能基准。
 */
public class TickRatePerformanceTest {
    private static final Logger log = LogManager.getLogger(TickRatePerformanceTest.class);
    private static final String TICK_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.tickrate";

    private EcsWorld ecsWorld;

    @BeforeEach
    void setUp() {
        ecsWorld = EcsWorld.generateInstance(TICK_PACKAGE);
    }

    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    @Test
    void testMixedTickRateSystemsUpdateMedium() {
        runBenchmark(2000, 200, 50.0);
    }

    @Test
    void testMixedTickRateSystemsUpdateLarge() {
        runBenchmark(8000, 80, 100.0);
    }

    private void runBenchmark(int entityCount, int iterations, double maxAvgMs) {
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(TickEntityFactory.class);
        }

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

        log.info("TickRate 混合系统基准 ({}实体, 预热{}次, 计时{}次): 总耗时 {} ms, 平均每次 {} ms ({}批中位数)",
                entityCount, warmup, iterations, totalTimeMs, avgTimeMs, measurementBatches);

        assertTrue(avgTimeMs < maxAvgMs,
                "Mixed TickRate update avg should be < " + maxAvgMs + "ms, was " + avgTimeMs);
    }
}
