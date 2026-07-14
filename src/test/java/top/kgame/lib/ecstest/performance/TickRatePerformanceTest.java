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

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ecsWorld.update((i + warmup) * 33L);
        }
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;

        log.info("TickRate 混合系统基准 ({}实体, 预热{}次, 计时{}次): 总耗时 {} ms, 平均每次 {} ms",
                entityCount, warmup, iterations, totalTimeMs, avgTimeMs);

        assertTrue(avgTimeMs < maxAvgMs,
                "Mixed TickRate update avg should be < " + maxAvgMs + "ms, was " + avgTimeMs);
    }
}
