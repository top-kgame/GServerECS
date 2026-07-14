package top.kgame.lib.ecstest.performance;

import top.kgame.lib.ecs.EcsWorld;

/**
 * ECS 性能测试共用预热逻辑，尽量降低 JIT 冷启动对计时窗口的影响。
 */
final class EcsPerformanceTestSupport {
    /** 至少执行的预热次数（world.update 帧，或独立微基准调用次数）。 */
    static final int MIN_WARMUP_ITERATIONS = 2000;
    /** 预热期间累计实体更新次数下限，兼顾并行 worker 上的方法热度。 */
    static final long MIN_WARMUP_ENTITY_OPS = 100_000L;
    /** 避免复杂逻辑 + 大规模实体时预热过久。 */
    static final int MAX_WARMUP_ITERATIONS = 5000;

    private EcsPerformanceTestSupport() {
    }

    static int resolveWarmupIterations(int entityCount) {
        if (entityCount <= 0) {
            return MIN_WARMUP_ITERATIONS;
        }
        long neededByEntityOps = (MIN_WARMUP_ENTITY_OPS + entityCount - 1) / entityCount;
        long warmup = Math.max(MIN_WARMUP_ITERATIONS, neededByEntityOps);
        return (int) Math.min(warmup, MAX_WARMUP_ITERATIONS);
    }

    /**
     * 对 world.update 热路径做不计时预热，并建议一次 GC。
     */
    static void warmupWorldUpdates(EcsWorld world, int warmupIterations) {
        for (int i = 0; i < warmupIterations; i++) {
            world.update(i * 33L);
        }
        System.gc();
    }

    /**
     * 按实体规模解析预热帧数并执行，返回实际预热次数。
     */
    static int warmupWorldForEntities(EcsWorld world, int entityCount) {
        int warmup = resolveWarmupIterations(entityCount);
        warmupWorldUpdates(world, warmup);
        return warmup;
    }

    /**
     * 微基准预热：执行给定次数的 runnable，再建议一次 GC。
     */
    static void warmupCallable(int warmupIterations, Runnable action) {
        for (int i = 0; i < warmupIterations; i++) {
            action.run();
        }
        System.gc();
    }

    /**
     * 微基准预热次数：至少 {@link #MIN_WARMUP_ITERATIONS}。
     */
    static int microBenchmarkWarmupIterations() {
        return MIN_WARMUP_ITERATIONS;
    }
}
