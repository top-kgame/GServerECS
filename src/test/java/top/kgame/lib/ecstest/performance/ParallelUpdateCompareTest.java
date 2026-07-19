package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.core.EntityFactory;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.ComplexPerfEntityFactory;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.SimplePerfEntityFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ParallelUpdate 对照测试：对比有/无 {@code @ParallelUpdate} 在简单逻辑与复杂逻辑下的耗时。
 * <p>
 * 正式计时前通过 {@link EcsPerformanceTestSupport} 做充分预热，以降低 JIT 冷启动对对比结果的影响。
 */
public class ParallelUpdateCompareTest {
    private static final Logger log = LogManager.getLogger(ParallelUpdateCompareTest.class);
    private static final String SHARED_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared";
    private static final String SIMPLE_PARALLEL_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.simple.parallel";
    private static final String SIMPLE_SERIAL_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.simple.serial";
    private static final String COMPLEX_PARALLEL_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.complex.parallel";
    private static final String COMPLEX_SERIAL_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.complex.serial";

    @Test
    void testSimpleLogicParallelVsSerial() {
        int entityCount = 4000;
        int iterations = 120;
        double maxAvgMs = 100.0;

        MeasureResult parallel = measure(
                "简单逻辑+ParallelUpdate",
                SIMPLE_PARALLEL_PACKAGE,
                SimplePerfEntityFactory.class,
                entityCount,
                iterations);
        MeasureResult serial = measure(
                "简单逻辑+串行",
                SIMPLE_SERIAL_PACKAGE,
                SimplePerfEntityFactory.class,
                entityCount,
                iterations);

        logComparison("简单逻辑", entityCount, iterations, parallel, serial);

        assertTrue(parallel.avgTimeMs < maxAvgMs,
                "Simple parallel avg should be < " + maxAvgMs + "ms, was " + parallel.avgTimeMs);
        assertTrue(serial.avgTimeMs < maxAvgMs,
                "Simple serial avg should be < " + maxAvgMs + "ms, was " + serial.avgTimeMs);
    }

    @Test
    void testComplexLogicParallelVsSerial() {
        int entityCount = 2000;
        int iterations = 80;
        double maxAvgMs = 500.0;

        MeasureResult parallel = measure(
                "复杂逻辑+ParallelUpdate",
                COMPLEX_PARALLEL_PACKAGE,
                ComplexPerfEntityFactory.class,
                entityCount,
                iterations);
        MeasureResult serial = measure(
                "复杂逻辑+串行",
                COMPLEX_SERIAL_PACKAGE,
                ComplexPerfEntityFactory.class,
                entityCount,
                iterations);

        logComparison("复杂逻辑", entityCount, iterations, parallel, serial);

        assertTrue(parallel.avgTimeMs < maxAvgMs,
                "Complex parallel avg should be < " + maxAvgMs + "ms, was " + parallel.avgTimeMs);
        assertTrue(serial.avgTimeMs < maxAvgMs,
                "Complex serial avg should be < " + maxAvgMs + "ms, was " + serial.avgTimeMs);
        // 复杂逻辑下并行不应明显劣于串行（允许调度抖动）
        assertTrue(parallel.avgTimeMs < serial.avgTimeMs * 1.5,
                "Complex parallel should not be much slower than serial. parallel="
                        + parallel.avgTimeMs + "ms, serial=" + serial.avgTimeMs + "ms");
    }

    private MeasureResult measure(String label,
                                  String systemPackage,
                                  Class<? extends EntityFactory> factoryClass,
                                  int entityCount,
                                  int iterations) {
        EcsWorld world = EcsWorld.generateInstance(SHARED_PACKAGE, systemPackage);
        try {
            for (int i = 0; i < entityCount; i++) {
                world.createEntity(factoryClass);
            }

            int warmup = EcsPerformanceTestSupport.warmupWorldForEntities(world, entityCount);

            int measurementBatches = 5;
            double[] batchAvgTimesMs = new double[measurementBatches];
            for (int batch = 0; batch < measurementBatches; batch++) {
                long startTime = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    int frame = warmup + batch * iterations + i;
                    world.update(frame * 33L);
                }
                long elapsedNs = System.nanoTime() - startTime;
                batchAvgTimesMs[batch] = elapsedNs / 1_000_000.0 / iterations;
            }

            double avgTimeMs = EcsPerformanceTestSupport.median(batchAvgTimesMs);
            double totalTimeMs = avgTimeMs * iterations;
            log.info("{} 测量结果 ({}个实体, 预热{}次, 计时{}次迭代): 总耗时 {} ms, 平均每次 {} ms ({}批中位数)",
                    label, entityCount, warmup, iterations, totalTimeMs, avgTimeMs,
                    measurementBatches);
            return new MeasureResult(totalTimeMs, avgTimeMs);
        } finally {
            if (!world.isClosed()) {
                world.close();
            }
        }
    }

    private void logComparison(String scenario,
                               int entityCount,
                               int iterations,
                               MeasureResult parallel,
                               MeasureResult serial) {
        double speedup = serial.avgTimeMs / parallel.avgTimeMs;
        // 加速比为两路测量之商，抖动会被放大；对比脚本按「仅参考」处理，不计入显著变好/变差
        log.info("{} 对照 ({}个实体, {}次迭代): 串行平均 {} ms, 并行平均 {} ms, 加速比(参考) {}",
                scenario, entityCount, iterations, serial.avgTimeMs, parallel.avgTimeMs, speedup);
    }

    private record MeasureResult(double totalTimeMs, double avgTimeMs) {
    }
}
