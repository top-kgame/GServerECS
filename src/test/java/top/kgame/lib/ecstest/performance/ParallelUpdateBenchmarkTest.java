package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.core.EntityFactory;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.ComplexPerfEntityFactory;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.MultiPerfEntityFactory;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.SimplePerfEntityFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ParallelUpdate 基准测试。
 * <p>
 * 覆盖：边界规模（0/1）、简单逻辑多规模、复杂逻辑多规模、
 * 多 ParallelUpdate 系统、双组件 ParallelUpdate 系统，并输出吞吐。
 * <p>
 * 正式计时前通过 {@link EcsPerformanceTestSupport} 做充分预热，以降低 JIT 冷启动对结果的影响。
 */
public class ParallelUpdateBenchmarkTest {
    private static final Logger log = LogManager.getLogger(ParallelUpdateBenchmarkTest.class);

    private static final String SHARED_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared";
    private static final String SIMPLE_BENCH_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.bench";
    private static final String COMPLEX_BENCH_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.benchcomplex";
    private static final String MULTI_BENCH_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.multi";
    private static final String TWO_COMPONENT_BENCH_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.twocomponent";

    private EcsWorld ecsWorld;

    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    // ---------- 边界：空列表跳过 / 单实体本线程快路径 ----------

    @Test
    void testParallelUpdateBenchmarkWithEmptyEntityList() {
        runBenchmark("边界-空实体", SHARED_PACKAGE, SIMPLE_BENCH_PACKAGE,
                SimplePerfEntityFactory.class, 0, 500, 5.0);
    }

    @Test
    void testParallelUpdateBenchmarkWithSingleEntity() {
        runBenchmark("边界-单实体快路径", SHARED_PACKAGE, SIMPLE_BENCH_PACKAGE,
                SimplePerfEntityFactory.class, 1, 1000, 5.0);
    }

    // ---------- 简单逻辑：不同实体规模 ----------

    @Test
    void testSimpleLogicBenchmarkWithSmallEntityCount() {
        runBenchmark("简单逻辑-小规模", SHARED_PACKAGE, SIMPLE_BENCH_PACKAGE,
                SimplePerfEntityFactory.class, 500, 200, 20.0);
    }

    @Test
    void testSimpleLogicBenchmarkWithMediumEntityCount() {
        runBenchmark("简单逻辑-中规模", SHARED_PACKAGE, SIMPLE_BENCH_PACKAGE,
                SimplePerfEntityFactory.class, 2000, 100, 80.0);
    }

    @Test
    void testSimpleLogicBenchmarkWithLargeEntityCount() {
        runBenchmark("简单逻辑-大规模", SHARED_PACKAGE, SIMPLE_BENCH_PACKAGE,
                SimplePerfEntityFactory.class, 8000, 50, 300.0);
    }

    // ---------- 复杂逻辑：不同实体规模 ----------

    @Test
    void testComplexLogicBenchmarkWithMediumEntityCount() {
        runBenchmark("复杂逻辑-中规模", SHARED_PACKAGE, COMPLEX_BENCH_PACKAGE,
                ComplexPerfEntityFactory.class, 1000, 50, 200.0);
    }

    @Test
    void testComplexLogicBenchmarkWithLargeEntityCount() {
        runBenchmark("复杂逻辑-大规模", SHARED_PACKAGE, COMPLEX_BENCH_PACKAGE,
                ComplexPerfEntityFactory.class, 4000, 60, 500.0);
    }

    // ---------- 多个 @ParallelUpdate 系统（系统间串行） ----------

    @Test
    void testMultipleParallelUpdateSystemsBenchmark() {
        runBenchmark("多ParallelUpdate系统", SHARED_PACKAGE, MULTI_BENCH_PACKAGE,
                MultiPerfEntityFactory.class, 4000, 50, 200.0);
    }

    // ---------- 双组件 ParallelUpdate 系统 ----------

    @Test
    void testTwoComponentParallelUpdateBenchmark() {
        runBenchmark("双组件ParallelUpdate", SHARED_PACKAGE, TWO_COMPONENT_BENCH_PACKAGE,
                MultiPerfEntityFactory.class, 4000, 50, 150.0);
    }

    private void runBenchmark(String scenario,
                              String sharedPackage,
                              String systemPackage,
                              Class<? extends EntityFactory> factoryClass,
                              int entityCount,
                              int iterations,
                              double maxAvgMs) {
        ecsWorld = EcsWorld.generateInstance(sharedPackage, systemPackage);
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(factoryClass);
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
            long elapsedNs = System.nanoTime() - startTime;
            batchAvgTimesMs[batch] = elapsedNs / 1_000_000.0 / iterations;
        }

        double avgTimeMs = EcsPerformanceTestSupport.median(batchAvgTimesMs);
        double totalTimeMs = avgTimeMs * iterations;
        double entitiesPerSecond = avgTimeMs <= 0
                ? Double.POSITIVE_INFINITY
                : entityCount * 1000.0 / avgTimeMs;

        log.info("ParallelUpdate 基准 [{}] ({}个实体, 预热{}次, 计时{}次迭代): 总耗时 {} ms, 平均每次 {} ms ({}批中位数), 吞吐约 {} 实体/秒",
                scenario, entityCount, warmup, iterations, totalTimeMs, avgTimeMs,
                measurementBatches, entitiesPerSecond);

        assertTrue(avgTimeMs < maxAvgMs,
                scenario + " avg time should be less than " + maxAvgMs + "ms, was " + avgTimeMs);
    }
}
