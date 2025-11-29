package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.core.EcsSystemManager;
import top.kgame.lib.ecs.core.SystemScheduler;
import top.kgame.lib.ecstest.core.system.*;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * System排序性能测试
 * 测试不同数量系统的排序性能
 */
public class SystemSortPerformanceTest {
    private static final Logger log = LogManager.getLogger(SystemSortPerformanceTest.class);
    private SystemScheduler scheduler;
    private EcsWorld ecsWorld;
    private EcsSystemManager systemManager;

    @BeforeEach
    void setUp() {
        scheduler = new SystemScheduler();
        ecsWorld = EcsWorld.generateInstance("top.kgame.lib.ecs.nonexistent");
        systemManager = new EcsSystemManager(ecsWorld);
    }

    /**
     * 创建系统实例
     */
    private EcsSystem createAndInitSystem(Class<? extends EcsSystem> systemClass) {
        try {
            EcsSystem system = systemClass.getConstructor().newInstance();
            system.init(systemManager);
            return system;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create system: " + systemClass.getName(), e);
        }
    }

    /**
     * 测试少量系统排序性能（10个系统）
     */
    @Test
    void testSortPerformanceWithSmallSystemCount() {
        int systemCount = 10;
        int iterations = 10000;
        
        // 准备系统
        Set<EcsSystem> systems = new LinkedHashSet<>();
        for (int i = 0; i < systemCount; i++) {
            systems.add(createAndInitSystem(TestSystemA.class));
        }
        
        // 预热
        for (int i = 0; i < 100; i++) {
            SystemScheduler testScheduler = new SystemScheduler();
            for (EcsSystem system : systems) {
                testScheduler.addSystem(system);
            }
            testScheduler.getSortedSystem();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            SystemScheduler testScheduler = new SystemScheduler();
            for (EcsSystem system : systems) {
                testScheduler.addSystem(system);
            }
            testScheduler.getSortedSystem();
        }
        long endTime = System.nanoTime();
        
        double avgTimeMs = (endTime - startTime) / 1_000_000.0 / iterations;
        log.info("少量系统排序性能测试 ({}个系统, {}次迭代): 平均耗时 {} ms", 
                systemCount, iterations, avgTimeMs);
        
        assertTrue(avgTimeMs < 1.0, "排序耗时应该小于1ms");
    }

    /**
     * 测试中等数量系统排序性能（50个系统）
     */
    @Test
    void testSortPerformanceWithMediumSystemCount() {
        int systemCount = 50;
        int iterations = 1000;
        
        // 准备系统
        Set<EcsSystem> systems = new LinkedHashSet<>();
        for (int i = 0; i < systemCount; i++) {
            systems.add(createAndInitSystem(TestSystemA.class));
        }
        
        // 预热
        for (int i = 0; i < 10; i++) {
            SystemScheduler testScheduler = new SystemScheduler();
            for (EcsSystem system : systems) {
                testScheduler.addSystem(system);
            }
            testScheduler.getSortedSystem();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            SystemScheduler testScheduler = new SystemScheduler();
            for (EcsSystem system : systems) {
                testScheduler.addSystem(system);
            }
            testScheduler.getSortedSystem();
        }
        long endTime = System.nanoTime();
        
        double avgTimeMs = (endTime - startTime) / 1_000_000.0 / iterations;
        log.info("中等数量系统排序性能测试 ({}个系统, {}次迭代): 平均耗时 {} ms", 
                systemCount, iterations, avgTimeMs);
        
        assertTrue(avgTimeMs < 5.0, "排序耗时应该小于5ms");
    }

    /**
     * 测试大量系统排序性能（200个系统）
     */
    @Test
    void testSortPerformanceWithLargeSystemCount() {
        int systemCount = 200;
        int iterations = 100;
        
        // 准备系统
        Set<EcsSystem> systems = new LinkedHashSet<>();
        for (int i = 0; i < systemCount; i++) {
            systems.add(createAndInitSystem(TestSystemA.class));
        }
        
        // 预热
        for (int i = 0; i < 5; i++) {
            SystemScheduler testScheduler = new SystemScheduler();
            for (EcsSystem system : systems) {
                testScheduler.addSystem(system);
            }
            testScheduler.getSortedSystem();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            SystemScheduler testScheduler = new SystemScheduler();
            for (EcsSystem system : systems) {
                testScheduler.addSystem(system);
            }
            testScheduler.getSortedSystem();
        }
        long endTime = System.nanoTime();
        
        double avgTimeMs = (endTime - startTime) / 1_000_000.0 / iterations;
        log.info("大量系统排序性能测试 ({}个系统, {}次迭代): 平均耗时 {} ms", 
                systemCount, iterations, avgTimeMs);
        
        assertTrue(avgTimeMs < 20.0, "排序耗时应该小于20ms");
    }

    /**
     * 测试有依赖关系的系统排序性能
     */
    @Test
    void testSortPerformanceWithDependencies() {
        int iterations = 1000;
        
        // 准备有依赖关系的系统
        Set<EcsSystem> systems = new LinkedHashSet<>();
        systems.add(createAndInitSystem(TestSystemA.class));
        systems.add(createAndInitSystem(TestSystemB.class));
        systems.add(createAndInitSystem(TestSystemC.class));
        systems.add(createAndInitSystem(TestSystemD.class));
        systems.add(createAndInitSystem(TestSystemE.class));
        systems.add(createAndInitSystem(TestSystemF.class));
        systems.add(createAndInitSystem(TestSystemG.class));
        
        // 预热
        for (int i = 0; i < 100; i++) {
            SystemScheduler testScheduler = new SystemScheduler();
            for (EcsSystem system : systems) {
                testScheduler.addSystem(system);
            }
            testScheduler.getSortedSystem();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            SystemScheduler testScheduler = new SystemScheduler();
            for (EcsSystem system : systems) {
                testScheduler.addSystem(system);
            }
            testScheduler.getSortedSystem();
        }
        long endTime = System.nanoTime();
        
        double avgTimeMs = (endTime - startTime) / 1_000_000.0 / iterations;
        log.info("有依赖关系的系统排序性能测试 ({}个系统, {}次迭代): 平均耗时 {} ms", 
                systems.size(), iterations, avgTimeMs);
        
        assertTrue(avgTimeMs < 1.0, "排序耗时应该小于1ms");
    }

    /**
     * 测试缓存效果 - 相同系统列表的重复排序
     */
    @Test
    void testSortPerformanceWithCache() {
        int iterations = 10000;
        
        // 准备系统
        Set<EcsSystem> systems = new LinkedHashSet<>();
        for (int i = 0; i < 20; i++) {
            systems.add(createAndInitSystem(TestSystemA.class));
        }
        
        // 第一次排序（无缓存）
        SystemScheduler firstScheduler = new SystemScheduler();
        for (EcsSystem system : systems) {
            firstScheduler.addSystem(system);
        }
        long startTime = System.nanoTime();
        firstScheduler.getSortedSystem();
        long firstTime = System.nanoTime() - startTime;
        
        // 后续排序（有缓存）
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            SystemScheduler testScheduler = new SystemScheduler();
            for (EcsSystem system : systems) {
                testScheduler.addSystem(system);
            }
            testScheduler.getSortedSystem();
        }
        long cachedTime = System.nanoTime() - startTime;
        
        double firstTimeMs = firstTime / 1_000_000.0;
        double avgCachedTimeMs = cachedTime / 1_000_000.0 / iterations;
        
        log.info("缓存效果测试: 首次排序耗时 {} ms, 缓存后平均耗时 {} ms (提升 {}倍)", 
                firstTimeMs, avgCachedTimeMs, firstTimeMs / avgCachedTimeMs);
        
        assertTrue(avgCachedTimeMs < firstTimeMs, "缓存应该提升性能");
    }
}
