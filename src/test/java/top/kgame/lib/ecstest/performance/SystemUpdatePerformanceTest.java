package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SystemUpdate性能测试
 * 测试系统更新的性能
 */
public class SystemUpdatePerformanceTest {
    private static final Logger log = LogManager.getLogger(SystemUpdatePerformanceTest.class);
    private EcsWorld ecsWorld;
    
    @BeforeEach
    void setUp() {
        ecsWorld = EcsWorld.generateInstance("top.kgame.lib.ecstest.util");
    }

    /**
     * 测试少量实体的系统更新性能
     */
    @Test
    void testSystemUpdatePerformanceWithSmallEntityCount() {
        int entityCount = 100;
        int iterations = 1000;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        
        // 预热
        for (int i = 0; i < 10; i++) {
            ecsWorld.update(i * 33);
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ecsWorld.update((i + 10) * 33);
        }
        long endTime = System.nanoTime();
        
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        
        log.info("少量实体系统更新性能测试 ({}个实体, {}次迭代): 总耗时 {} ms, 平均每次 {} ms", 
                entityCount, iterations, totalTimeMs, avgTimeMs);
        
        assertTrue(avgTimeMs < 10.0, "System update time should be less than 10ms");
    }

    /**
     * 测试中等数量实体的系统更新性能
     */
    @Test
    void testSystemUpdatePerformanceWithMediumEntityCount() {
        int entityCount = 1000;
        int iterations = 100;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        
        // 预热
        for (int i = 0; i < 5; i++) {
            ecsWorld.update(i * 33);
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ecsWorld.update((i + 5) * 33);
        }
        long endTime = System.nanoTime();
        
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        
        log.info("中等数量实体系统更新性能测试 ({}个实体, {}次迭代): 总耗时 {} ms, 平均每次 {} ms", 
                entityCount, iterations, totalTimeMs, avgTimeMs);
        
        assertTrue(avgTimeMs < 50.0, "System update time should be less than 50ms");
    }

    /**
     * 测试大量实体的系统更新性能
     */
    @Test
    void testSystemUpdatePerformanceWithLargeEntityCount() {
        int entityCount = 10000;
        int iterations = 10;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        
        // 预热
        for (int i = 0; i < 3; i++) {
            ecsWorld.update(i * 33);
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ecsWorld.update((i + 3) * 33);
        }
        long endTime = System.nanoTime();
        
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        
        log.info("大量实体系统更新性能测试 ({}个实体, {}次迭代): 总耗时 {} ms, 平均每次 {} ms", 
                entityCount, iterations, totalTimeMs, avgTimeMs);
        
        assertTrue(avgTimeMs < 500.0, "System update time should be less than 500ms");
    }

    /**
     * 测试多个系统的更新性能
     */
    @Test
    void testMultipleSystemUpdatePerformance() {
        int entityCount = 500;
        int iterations = 100;
        
        // 创建不同类型的实体
        for (int i = 0; i < entityCount; i++) {
            if (i % 3 == 0) {
                ecsWorld.createEntity(EntityIndex.E1.getId());
            } else if (i % 3 == 1) {
                ecsWorld.createEntity(EntityIndex.E12.getId());
            } else {
                ecsWorld.createEntity(EntityIndex.E123.getId());
            }
        }
        
        // 预热
        for (int i = 0; i < 5; i++) {
            ecsWorld.update(i * 33);
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ecsWorld.update((i + 5) * 33);
        }
        long endTime = System.nanoTime();
        
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        
        log.info("多个系统更新性能测试 ({}个实体, {}次迭代): 总耗时 {} ms, 平均每次 {} ms", 
                entityCount, iterations, totalTimeMs, avgTimeMs);
        
        assertTrue(avgTimeMs < 30.0, "Multiple system update time should be less than 30ms");
    }

    /**
     * 测试系统更新频率对性能的影响
     */
    @Test
    void testSystemUpdateFrequencyPerformance() {
        int entityCount = 1000;
        int totalUpdates = 1000;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        
        // 预热
        for (int i = 0; i < 10; i++) {
            ecsWorld.update(i * 33);
        }
        
        // 性能测试 - 连续更新
        long startTime = System.nanoTime();
        for (int i = 0; i < totalUpdates; i++) {
            ecsWorld.update((i + 10) * 33);
        }
        long endTime = System.nanoTime();
        
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / totalUpdates;
        double updatesPerSecond = 1000.0 / avgTimeMs;
        
        log.info("系统更新频率性能测试 ({}个实体, {}次更新): 总耗时 {} ms, 平均每次 {} ms, 理论每秒更新次数 {}", 
                entityCount, totalUpdates, totalTimeMs, avgTimeMs, updatesPerSecond);
        
        assertTrue(avgTimeMs < 5.0, "System update time should be less than 5ms");
        assertTrue(updatesPerSecond > 100, "Theoretical update frequency should be greater than 100 times/second");
    }
}
