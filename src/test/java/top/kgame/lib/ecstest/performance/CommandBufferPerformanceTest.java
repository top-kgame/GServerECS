package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.command.EcsCommandAddComponent;
import top.kgame.lib.ecs.command.EcsCommandCreateEntity;
import top.kgame.lib.ecs.command.EcsCommandDestroyEntity;
import top.kgame.lib.ecs.command.EcsCommandRemoveComponent;
import top.kgame.lib.ecstest.performance.isolated.lifecycle.LifecycleEntityFactory;
import top.kgame.lib.ecstest.performance.isolated.mutation.ComponentMutationTag;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * WORLD 作用域 Command Buffer 性能基准：批量 delay command + update flush。
 */
public class CommandBufferPerformanceTest {
    private static final Logger log = LogManager.getLogger(CommandBufferPerformanceTest.class);

    private EcsWorld ecsWorld;
    private long timeCursor;

    @BeforeEach
    void setUp() {
        ecsWorld = EcsWorld.generateInstance(
                "top.kgame.lib.ecstest.performance.isolated.lifecycle",
                "top.kgame.lib.ecstest.performance.isolated.mutation");
        timeCursor = 0L;
    }

    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    @Test
    void testBatchCreateCommandFlush() {
        int batchSize = 4000;
        int rounds = 30;
        warmupCreateDestroyCommands(Math.min(batchSize, 500));

        long startTime = System.nanoTime();
        for (int r = 0; r < rounds; r++) {
            for (int i = 0; i < batchSize; i++) {
                ecsWorld.addDelayCommand(new EcsCommandCreateEntity(
                        ecsWorld, LifecycleEntityFactory.TYPE_ID, entity -> {
                        }));
            }
            tick();
        }
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgRoundMs = totalTimeMs / rounds;
        double createsPerSecond = batchSize * (double) rounds * 1000.0 / totalTimeMs;

        log.info("CommandBuffer create 基准 (每轮{}条, {}轮): 总耗时 {} ms, 平均每轮 {} ms, 吞吐约 {} cmd/秒",
                batchSize, rounds, totalTimeMs, avgRoundMs, createsPerSecond);

        assertTrue(avgRoundMs < 150.0,
                "Create command round avg should be < 150ms, was " + avgRoundMs);
    }

    @Test
    void testBatchDestroyCommandFlush() {
        int batchSize = 4000;
        warmupCreateDestroyCommands(Math.min(batchSize, 500));

        List<EcsEntity> entities = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            entities.add(ecsWorld.createEntity(LifecycleEntityFactory.class));
        }

        long startTime = System.nanoTime();
        for (EcsEntity entity : entities) {
            ecsWorld.addDelayCommand(new EcsCommandDestroyEntity(ecsWorld, entity));
        }
        tick();
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double destroysPerSecond = batchSize * 1000.0 / totalTimeMs;

        log.info("CommandBuffer destroy 基准 ({}条+update): 总耗时 {} ms, 吞吐约 {} cmd/秒",
                batchSize, totalTimeMs, destroysPerSecond);

        assertTrue(totalTimeMs < 200.0,
                "Destroy command flush should be < 200ms, was " + totalTimeMs);
    }

    @Test
    void testBatchAddRemoveComponentCommandFlush() {
        int entityCount = 3000;
        List<EcsEntity> entities = new ArrayList<>(entityCount);
        for (int i = 0; i < entityCount; i++) {
            entities.add(ecsWorld.createEntity(LifecycleEntityFactory.class));
        }

        int warmupRounds = 80;
        for (int r = 0; r < warmupRounds; r++) {
            for (EcsEntity entity : entities) {
                ecsWorld.addDelayCommand(new EcsCommandAddComponent(entity, new ComponentMutationTag()));
            }
            tick();
            for (EcsEntity entity : entities) {
                ecsWorld.addDelayCommand(new EcsCommandRemoveComponent(entity, ComponentMutationTag.class));
            }
            tick();
        }
        System.gc();

        long startTime = System.nanoTime();
        for (EcsEntity entity : entities) {
            ecsWorld.addDelayCommand(new EcsCommandAddComponent(entity, new ComponentMutationTag()));
        }
        tick();
        for (EcsEntity entity : entities) {
            ecsWorld.addDelayCommand(new EcsCommandRemoveComponent(entity, ComponentMutationTag.class));
        }
        tick();
        long endTime = System.nanoTime();

        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double cmdsPerSecond = entityCount * 2.0 * 1000.0 / totalTimeMs;

        log.info("CommandBuffer add/remove component 基准 ({}实体×2命令+flush): 总耗时 {} ms, 吞吐约 {} cmd/秒",
                entityCount, totalTimeMs, cmdsPerSecond);

        assertTrue(totalTimeMs < 200.0,
                "Add/remove component commands should be < 200ms, was " + totalTimeMs);
    }

    @Test
    void testCommandCreateVsDirectCreateCompare() {
        int batchSize = 2000;

        warmupCreateDestroyCommands(500);

        long commandStart = System.nanoTime();
        for (int i = 0; i < batchSize; i++) {
            ecsWorld.addDelayCommand(new EcsCommandCreateEntity(
                    ecsWorld, LifecycleEntityFactory.TYPE_ID, entity -> {
                    }));
        }
        tick();
        double commandMs = (System.nanoTime() - commandStart) / 1_000_000.0;

        long directStart = System.nanoTime();
        for (int i = 0; i < batchSize; i++) {
            ecsWorld.createEntity(LifecycleEntityFactory.class);
        }
        double directMs = (System.nanoTime() - directStart) / 1_000_000.0;

        log.info("Command vs Direct create 对照 ({}个): command+flush {} ms, direct {} ms, 比值 {}",
                batchSize, commandMs, directMs, commandMs / directMs);

        assertTrue(commandMs < 150.0, "Command create path should be < 150ms, was " + commandMs);
        assertTrue(directMs < 150.0, "Direct create path should be < 150ms, was " + directMs);
    }

    private void warmupCreateDestroyCommands(int batchSize) {
        int rounds = 100;
        for (int r = 0; r < rounds; r++) {
            List<EcsEntity> created = new ArrayList<>(batchSize);
            for (int i = 0; i < batchSize; i++) {
                ecsWorld.addDelayCommand(new EcsCommandCreateEntity(
                        ecsWorld, LifecycleEntityFactory.TYPE_ID, created::add));
            }
            tick();
            for (EcsEntity entity : created) {
                ecsWorld.addDelayCommand(new EcsCommandDestroyEntity(ecsWorld, entity));
            }
            tick();
        }
        System.gc();
    }

    private void tick() {
        timeCursor += 33L;
        ecsWorld.update(timeCursor);
    }
}
