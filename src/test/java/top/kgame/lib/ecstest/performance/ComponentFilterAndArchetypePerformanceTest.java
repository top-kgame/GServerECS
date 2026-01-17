package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.core.ComponentFilter;
import top.kgame.lib.ecs.core.ComponentFilterParam;
import top.kgame.lib.ecs.core.EntityArchetype;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.Component4;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ComponentFilter和EntityArchetype性能测试
 * 测试组件过滤器和实体原型的匹配性能
 */
public class ComponentFilterAndArchetypePerformanceTest {
    private static final Logger log = LogManager.getLogger(ComponentFilterAndArchetypePerformanceTest.class);
    private EcsWorld ecsWorld;
    private List<EntityArchetype> archetypes;

    @BeforeEach
    void setUp() {
        ecsWorld = EcsWorld.generateInstance("top.kgame.lib.ecstest.util");
        
        // 创建多个不同的Archetype - 通过创建实体来生成Archetype
        archetypes = new ArrayList<>();
        
        // 创建实体以生成不同的Archetype
        // Archetype 1: 只有Component1
        EcsEntity entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        archetypes.add(entity1.getArchetype());
        
        // Archetype 2: Component1 + Component2  
        EcsEntity entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
        archetypes.add(entity12.getArchetype());
        
        // Archetype 3: Component1 + Component2 + Component3
        EcsEntity entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        archetypes.add(entity123.getArchetype());
        
        // 确保Archetype唯一
        Set<EntityArchetype> uniqueArchetypes = new LinkedHashSet<>(archetypes);
        archetypes = new ArrayList<>(uniqueArchetypes);
    }

    /**
     * 测试ComponentFilter生成性能
     */
    @Test
    void testComponentFilterGenerationPerformance() {
        int iterations = 100000;
        
        List<ComponentFilterParam<?>> params = Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        );
        
        // 预热
        for (int i = 0; i < 1000; i++) {
            ComponentFilter.generate(ecsWorld, params);
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            ComponentFilter.generate(ecsWorld, params);
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("ComponentFilter生成性能测试 ({}次迭代): 平均耗时 {} ms ({} ns)", 
                iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 1000, "Generation time should be less than 1000ns");
    }

    /**
     * 测试单个Archetype匹配性能
     */
    @Test
    void testSingleArchetypeMatchingPerformance() {
        int iterations = 1000000;
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        EntityArchetype targetArchetype = archetypes.get(1); // Component1 + Component2
        
        // 预热
        for (int i = 0; i < 10000; i++) {
            filter.isMatchingArchetype(targetArchetype);
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            filter.isMatchingArchetype(targetArchetype);
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("单个Archetype匹配性能测试 ({}次迭代): 平均耗时 {} ms ({} ns)", 
                iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 100, "Matching time should be less than 100ns");
    }

    /**
     * 测试多个Archetype匹配性能
     */
    @Test
    void testMultipleArchetypeMatchingPerformance() {
        int iterations = 100000;
        int archetypeCount = archetypes.size();
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        // 预热
        for (int i = 0; i < 1000; i++) {
            for (EntityArchetype archetype : archetypes) {
                filter.isMatchingArchetype(archetype);
            }
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            for (EntityArchetype archetype : archetypes) {
                filter.isMatchingArchetype(archetype);
            }
        }
        long endTime = System.nanoTime();
        
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        double avgTimePerArchetypeMs = avgTimeMs / archetypeCount;
        
        log.info("多个Archetype匹配性能测试 ({}个Archetype, {}次迭代): 总耗时 {} ms, 平均每次 {} ms, 每个Archetype {} ms", 
                archetypeCount, iterations, totalTimeMs, avgTimeMs, avgTimePerArchetypeMs);
        
        assertTrue(avgTimePerArchetypeMs < 0.001, "Matching time per Archetype should be less than 0.001ms");
    }

    /**
     * 测试复杂Filter匹配性能（包含require、anyOf、exclude）
     */
    @Test
    void testComplexFilterMatchingPerformance() {
        int iterations = 100000;
        
        ComponentFilter complexFilter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class),
            ComponentFilterParam.anyOf(Component3.class),
            ComponentFilterParam.exclude(Component4.class)
        ));
        
        // 预热
        for (int i = 0; i < 1000; i++) {
            for (EntityArchetype archetype : archetypes) {
                complexFilter.isMatchingArchetype(archetype);
            }
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            for (EntityArchetype archetype : archetypes) {
                complexFilter.isMatchingArchetype(archetype);
            }
        }
        long endTime = System.nanoTime();
        
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        
        log.info("复杂Filter匹配性能测试 ({}个Archetype, {}次迭代): 总耗时 {} ms, 平均每次 {} ms", 
                archetypes.size(), iterations, totalTimeMs, avgTimeMs);
        
        assertTrue(avgTimeMs < 0.01, "Complex matching time should be less than 0.01ms");
    }

    /**
     * 测试大量Archetype的匹配性能
     */
    @Test
    void testLargeArchetypeSetMatchingPerformance() {
        int largeArchetypeCount = 1000;
        int iterations = 1000;
        
        // 创建大量Archetype - 通过创建实体来生成
        List<EntityArchetype> largeArchetypes = new ArrayList<>();
        Set<EntityArchetype> seenArchetypes = new HashSet<>();
        
        for (int i = 0; i < largeArchetypeCount; i++) {
            EcsEntity entity;
            if (i % 3 == 0) {
                entity = ecsWorld.createEntity(EntityIndex.E1.getId());
            } else if (i % 3 == 1) {
                entity = ecsWorld.createEntity(EntityIndex.E12.getId());
            } else {
                entity = ecsWorld.createEntity(EntityIndex.E123.getId());
            }
            
            EntityArchetype archetype = entity.getArchetype();
            if (seenArchetypes.add(archetype)) {
                largeArchetypes.add(archetype);
            }
            
            // 如果已经有足够的唯一Archetype，停止创建
            if (largeArchetypes.size() >= largeArchetypeCount) {
                break;
            }
        }
        
        // 如果还不够，重复使用已有的Archetype
        while (largeArchetypes.size() < largeArchetypeCount) {
            largeArchetypes.add(largeArchetypes.get(largeArchetypes.size() % 3));
        }
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        // 预热
        for (int i = 0; i < 10; i++) {
            for (EntityArchetype archetype : largeArchetypes) {
                filter.isMatchingArchetype(archetype);
            }
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            for (EntityArchetype archetype : largeArchetypes) {
                filter.isMatchingArchetype(archetype);
            }
        }
        long endTime = System.nanoTime();
        
        double totalTimeMs = (endTime - startTime) / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        double avgTimePerArchetypeMs = avgTimeMs / largeArchetypeCount;
        
        log.info("大量Archetype匹配性能测试 ({}个Archetype, {}次迭代): 总耗时 {} ms, 平均每次 {} ms, 每个Archetype {} ms", 
                largeArchetypeCount, iterations, totalTimeMs, avgTimeMs, avgTimePerArchetypeMs);
        
        assertTrue(avgTimePerArchetypeMs < 0.001, "Matching time per Archetype should be less than 0.001ms");
    }
}
