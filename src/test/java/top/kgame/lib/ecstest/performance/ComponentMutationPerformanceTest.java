package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecstest.performance.isolated.mutation.ComponentMutationTag;
import top.kgame.lib.ecstest.performance.isolated.mutation.MutationEntityFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 组件增删 / Archetype 迁移性能基准。
 */
public class ComponentMutationPerformanceTest {
    private static final Logger log = LogManager.getLogger(ComponentMutationPerformanceTest.class);
    private static final String MUTATION_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.mutation";

    private EcsWorld ecsWorld;
    private long timeCursor;

    @BeforeEach
    void setUp() {
        ecsWorld = EcsWorld.generateInstance(MUTATION_PACKAGE);
        timeCursor = 0L;
    }

    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    @Test
    void testSingleEntityAddRemoveComponentToggle() {
        EcsEntity entity = ecsWorld.createEntity(MutationEntityFactory.class);
        int iterations = 20000;
        warmupToggle(entity, EcsPerformanceTestSupport.microBenchmarkWarmupIterations());

        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            entity.addComponent(new ComponentMutationTag());
            entity.removeComponent(ComponentMutationTag.class);
        }
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgToggleUs = totalTimeMs * 1000.0 / iterations;

        log.info("单实体 add/remove 切换基准 ({}次): 总耗时 {} ms, 平均每次 {} us",
                iterations, totalTimeMs, avgToggleUs);

        assertTrue(avgToggleUs < 50.0,
                "Single entity toggle avg should be < 50us, was " + avgToggleUs);
    }

    @Test
    void testBatchEntityComponentMigration() {
        int entityCount = 4000;
        EcsEntity[] entities = new EcsEntity[entityCount];
        for (int i = 0; i < entityCount; i++) {
            entities[i] = ecsWorld.createEntity(MutationEntityFactory.class);
        }

        EcsPerformanceTestSupport.warmupCallable(200, () -> {
            for (EcsEntity entity : entities) {
                entity.addComponent(new ComponentMutationTag());
            }
            for (EcsEntity entity : entities) {
                entity.removeComponent(ComponentMutationTag.class);
            }
        });

        long startTime = System.nanoTime();
        for (EcsEntity entity : entities) {
            entity.addComponent(new ComponentMutationTag());
        }
        for (EcsEntity entity : entities) {
            entity.removeComponent(ComponentMutationTag.class);
        }
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double migrationsPerSecond = entityCount * 2.0 * 1000.0 / totalTimeMs;

        log.info("批量实体组件迁移基准 ({}实体 add+remove): 总耗时 {} ms, 吞吐约 {} 次迁移/秒",
                entityCount, totalTimeMs, migrationsPerSecond);

        assertTrue(totalTimeMs < 200.0,
                "Batch migration total should be < 200ms, was " + totalTimeMs);
    }

    @Test
    void testMigrationThenWorldUpdate() {
        int entityCount = 2000;
        int updateIterations = 100;
        EcsEntity[] entities = new EcsEntity[entityCount];
        for (int i = 0; i < entityCount; i++) {
            entities[i] = ecsWorld.createEntity(MutationEntityFactory.class);
        }

        // 预热迁移 + update（继续沿用 timeCursor，避免回绕时间戳）
        int warmupRounds = 100;
        for (int i = 0; i < warmupRounds; i++) {
            for (EcsEntity entity : entities) {
                entity.addComponent(new ComponentMutationTag());
            }
            tick();
            for (EcsEntity entity : entities) {
                entity.removeComponent(ComponentMutationTag.class);
            }
            tick();
        }
        System.gc();

        for (EcsEntity entity : entities) {
            entity.addComponent(new ComponentMutationTag());
        }
        for (int i = 0; i < 50; i++) {
            tick();
        }

        long startTime = System.nanoTime();
        for (int i = 0; i < updateIterations; i++) {
            tick();
        }
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgUpdateMs = totalTimeMs / updateIterations;

        log.info("迁移后 world.update 基准 ({}实体带额外组件, 预热迁移{}轮, {}次update): 平均每次 {} ms",
                entityCount, warmupRounds, updateIterations, avgUpdateMs);

        assertTrue(avgUpdateMs < 20.0,
                "Update after migration avg should be < 20ms, was " + avgUpdateMs);
    }

    private void warmupToggle(EcsEntity entity, int rounds) {
        EcsPerformanceTestSupport.warmupCallable(rounds, () -> {
            entity.addComponent(new ComponentMutationTag());
            entity.removeComponent(ComponentMutationTag.class);
        });
    }

    private void tick() {
        timeCursor += 33L;
        ecsWorld.update(timeCursor);
    }
}
