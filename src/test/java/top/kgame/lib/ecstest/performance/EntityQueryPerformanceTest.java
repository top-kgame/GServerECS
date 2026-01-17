package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.core.ComponentFilter;
import top.kgame.lib.ecs.core.ComponentFilterParam;
import top.kgame.lib.ecs.core.EntityQuery;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EntityQuery性能测试
 * 测试实体查询的性能
 */
public class EntityQueryPerformanceTest {
    private static final Logger log = LogManager.getLogger(EntityQueryPerformanceTest.class);
    private EcsWorld ecsWorld;
    private Method findOrCreateEntityQueryMethod;

    @BeforeEach
    void setUp() throws Exception {
        ecsWorld = EcsWorld.generateInstance("top.kgame.lib.ecstest.util");
        // 使用反射访问包级私有方法
        findOrCreateEntityQueryMethod = EcsWorld.class.getDeclaredMethod("findOrCreateEntityQuery", ComponentFilter.class);
        findOrCreateEntityQueryMethod.setAccessible(true);
    }

    /**
     * 通过反射调用findOrCreateEntityQuery方法
     */
    private EntityQuery findOrCreateEntityQuery(ComponentFilter filter) {
        try {
            return (EntityQuery) findOrCreateEntityQueryMethod.invoke(ecsWorld, filter);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke findOrCreateEntityQuery", e);
        }
    }

    /**
     * 测试EntityQuery创建性能
     */
    @Test
    void testEntityQueryCreationPerformance() {
        int iterations = 100000;
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        // 预热
        for (int i = 0; i < 1000; i++) {
            findOrCreateEntityQuery(filter);
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            findOrCreateEntityQuery(filter);
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("EntityQuery创建性能测试 ({}次迭代): 平均耗时 {} ms ({} ns)", 
                iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 5000, "Creation time should be less than 5000ns");
    }

    /**
     * 测试EntityQuery查询少量实体性能
     */
    @Test
    void testEntityQueryPerformanceWithSmallEntityCount() {
        int entityCount = 100;
        int iterations = 10000;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E12.getId()); // Component1 + Component2
        }
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        EntityQuery query = findOrCreateEntityQuery(filter);
        
        // 预热
        for (int i = 0; i < 100; i++) {
            query.getEntityList();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            query.getEntityList();
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("少量实体查询性能测试 ({}个实体, {}次迭代): 平均耗时 {} ms ({} ns)", 
                entityCount, iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 10000, "Query time should be less than 10000ns");
    }

    /**
     * 测试EntityQuery查询中等数量实体性能
     */
    @Test
    void testEntityQueryPerformanceWithMediumEntityCount() {
        int entityCount = 1000;
        int iterations = 1000;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E12.getId()); // Component1 + Component2
        }
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        EntityQuery query = findOrCreateEntityQuery(filter);
        
        // 预热
        for (int i = 0; i < 10; i++) {
            query.getEntityList();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            query.getEntityList();
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("中等数量实体查询性能测试 ({}个实体, {}次迭代): 平均耗时 {} ms ({} ns)", 
                entityCount, iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 100000, "Query time should be less than 100000ns");
    }

    /**
     * 测试EntityQuery查询大量实体性能
     */
    @Test
    void testEntityQueryPerformanceWithLargeEntityCount() {
        int entityCount = 10000;
        int iterations = 100;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E12.getId()); // Component1 + Component2
        }
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        EntityQuery query = findOrCreateEntityQuery(filter);
        
        // 预热
        for (int i = 0; i < 5; i++) {
            query.getEntityList();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            query.getEntityList();
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("大量实体查询性能测试 ({}个实体, {}次迭代): 平均耗时 {} ms ({} ns)", 
                entityCount, iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 1000000, "Query time should be less than 1000000ns");
    }

    /**
     * 测试EntityQuery获取组件列表性能
     */
    @Test
    void testEntityQueryGetComponentListPerformance() {
        int entityCount = 1000;
        int iterations = 1000;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E12.getId()); // Component1 + Component2
        }
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        EntityQuery query = findOrCreateEntityQuery(filter);
        
        // 预热
        for (int i = 0; i < 10; i++) {
            query.getComponentDataList(Component1.class);
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            List<Component1> components = query.getComponentDataList(Component1.class);
            // 确保结果被使用，避免被优化掉
            if (components.size() != entityCount) {
                throw new AssertionError("组件数量不匹配");
            }
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("获取组件列表性能测试 ({}个实体, {}次迭代): 平均耗时 {} ms ({} ns)", 
                entityCount, iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 200000, "Get component list time should be less than 200000ns");
    }

    /**
     * 测试EntityQuery实体计数性能
     */
    @Test
    void testEntityQueryEntityCountPerformance() {
        int entityCount = 1000;
        int iterations = 100000;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E12.getId()); // Component1 + Component2
        }
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        EntityQuery query = findOrCreateEntityQuery(filter);
        
        // 预热
        for (int i = 0; i < 1000; i++) {
            query.entityCount();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            int count = query.entityCount();
            // 确保结果被使用，避免被优化掉
            if (count != entityCount) {
                throw new AssertionError("实体数量不匹配");
            }
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("实体计数性能测试 ({}个实体, {}次迭代): 平均耗时 {} ms ({} ns)", 
                entityCount, iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 1000, "Entity count time should be less than 1000ns");
    }

    /**
     * 测试EntityQuery isEmpty性能
     */
    @Test
    void testEntityQueryIsEmptyPerformance() {
        int entityCount = 1000;
        int iterations = 100000;
        
        // 创建实体
        for (int i = 0; i < entityCount; i++) {
            ecsWorld.createEntity(EntityIndex.E12.getId()); // Component1 + Component2
        }
        
        ComponentFilter filter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class)
        ));
        
        EntityQuery query = findOrCreateEntityQuery(filter);
        
        // 预热
        for (int i = 0; i < 1000; i++) {
            query.isEmpty();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            boolean empty = query.isEmpty();
            // 确保结果被使用，避免被优化掉
            if (empty) {
                throw new AssertionError("查询不应该为空");
            }
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("isEmpty性能测试 ({}个实体, {}次迭代): 平均耗时 {} ms ({} ns)", 
                entityCount, iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 1000, "isEmpty time should be less than 1000ns");
    }

    /**
     * 测试复杂查询条件性能
     */
    @Test
    void testComplexEntityQueryPerformance() {
        int entityCount = 1000;
        int iterations = 1000;
        
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
        
        // 复杂查询：需要Component1和Component2，任意一个Component3，排除Component4
        ComponentFilter complexFilter = ComponentFilter.generate(ecsWorld, Arrays.asList(
            ComponentFilterParam.require(Component1.class),
            ComponentFilterParam.require(Component2.class),
            ComponentFilterParam.anyOf(Component3.class)
        ));
        
        EntityQuery query = findOrCreateEntityQuery(complexFilter);
        
        // 预热
        for (int i = 0; i < 10; i++) {
            query.getEntityList();
        }
        
        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            List<EcsEntity> entities = query.getEntityList();
            // 确保结果被使用
            if (entities.isEmpty()) {
                throw new AssertionError("查询结果不应该为空");
            }
        }
        long endTime = System.nanoTime();
        
        double avgTimeNs = (endTime - startTime) / (double) iterations;
        double avgTimeMs = avgTimeNs / 1_000_000.0;
        
        log.info("复杂查询性能测试 ({}个实体, {}次迭代): 平均耗时 {} ms ({} ns)", 
                entityCount, iterations, avgTimeMs, avgTimeNs);
        
        assertTrue(avgTimeNs < 100000, "Complex query time should be less than 100000ns");
    }
}
