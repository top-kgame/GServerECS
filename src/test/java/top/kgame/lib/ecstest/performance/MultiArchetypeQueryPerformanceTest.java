package top.kgame.lib.ecstest.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.core.ComponentFilter;
import top.kgame.lib.ecs.core.ComponentFilterParam;
import top.kgame.lib.ecs.core.EntityArchetype;
import top.kgame.lib.ecs.core.EntityQuery;
import top.kgame.lib.ecstest.performance.isolated.archetype.ArchetypeBaseEntityFactory;
import top.kgame.lib.ecstest.performance.isolated.archetype.ArchetypeTagBank;
import top.kgame.lib.ecstest.performance.isolated.archetype.ComponentArchetypeBase;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 多唯一 Archetype 分布下的 Query / Filter 匹配性能基准。
 */
public class MultiArchetypeQueryPerformanceTest {
    private static final Logger log = LogManager.getLogger(MultiArchetypeQueryPerformanceTest.class);
    private static final String ARCHETYPE_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.archetype";

    private EcsWorld ecsWorld;
    private Method findOrCreateEntityQueryMethod;

    @BeforeEach
    void setUp() throws Exception {
        ecsWorld = EcsWorld.generateInstance(ARCHETYPE_PACKAGE);
        findOrCreateEntityQueryMethod = EcsWorld.class.getDeclaredMethod(
                "findOrCreateEntityQuery", ComponentFilter.class);
        findOrCreateEntityQueryMethod.setAccessible(true);
    }

    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    @Test
    void testGetEntityListAcrossManyUniqueArchetypes() throws Exception {
        int uniqueArchetypes = 64; // 6 bit 掩码
        int entitiesPerArchetype = 20;
        int uniqueCount = populateUniqueArchetypes(uniqueArchetypes, entitiesPerArchetype);
        assertTrue(uniqueCount >= 60, "Should create nearly 64 unique archetypes, got " + uniqueCount);

        ComponentFilter filter = ComponentFilter.generate(ecsWorld, List.of(
                ComponentFilterParam.require(ComponentArchetypeBase.class)
        ));
        EntityQuery query = findOrCreateEntityQuery(filter);

        int iterations = 2000;
        EcsPerformanceTestSupport.warmupCallable(
                EcsPerformanceTestSupport.microBenchmarkWarmupIterations(),
                query::getEntityList);

        long startTime = System.nanoTime();
        int lastSize = 0;
        for (int i = 0; i < iterations; i++) {
            lastSize = query.getEntityList().size();
        }
        long endTime = System.nanoTime();

        double avgTimeNs = (endTime - startTime) / (double) iterations;
        log.info("多唯一Archetype Query.getEntityList 基准 ({}独特Archetype, {}实体, {}次): 平均 {} ns, lastSize={}",
                uniqueCount, uniqueArchetypes * entitiesPerArchetype, iterations, avgTimeNs, lastSize);

        assertTrue(lastSize == uniqueArchetypes * entitiesPerArchetype);
        assertTrue(avgTimeNs < 500_000,
                "getEntityList avg should be < 500000ns, was " + avgTimeNs);
    }

    @Test
    void testFilterMatchAcrossManyUniqueArchetypes() {
        int uniqueArchetypes = 64;
        int entitiesPerArchetype = 1;
        int uniqueCount = populateUniqueArchetypes(uniqueArchetypes, entitiesPerArchetype);

        Set<EntityArchetype> archetypes = new HashSet<>();
        for (EcsEntity entity : ecsWorld.getAllEntity()) {
            archetypes.add(entity.getArchetype());
        }
        assertTrue(archetypes.size() >= 60, "unique archetypes=" + archetypes.size());

        ComponentFilter filter = ComponentFilter.generate(ecsWorld, List.of(
                ComponentFilterParam.require(ComponentArchetypeBase.class),
                ComponentFilterParam.require(ArchetypeTagBank.T0.class)
        ));

        EcsPerformanceTestSupport.warmupCallable(
                EcsPerformanceTestSupport.microBenchmarkWarmupIterations(),
                () -> {
                    for (EntityArchetype archetype : archetypes) {
                        filter.isMatchingArchetype(archetype);
                    }
                });

        int iterations = 20000;
        long startTime = System.nanoTime();
        int matchCount = 0;
        for (int i = 0; i < iterations; i++) {
            matchCount = 0;
            for (EntityArchetype archetype : archetypes) {
                if (filter.isMatchingArchetype(archetype)) {
                    matchCount++;
                }
            }
        }
        long endTime = System.nanoTime();

        double avgSweepNs = (endTime - startTime) / (double) iterations;
        log.info("多唯一Archetype Filter 扫描基准 ({}独特Archetype, {}次全扫): 平均每次扫 {} ns, 命中约 {}",
                uniqueCount, iterations, avgSweepNs, matchCount);

        assertTrue(matchCount > 0);
        assertTrue(avgSweepNs < 50_000,
                "filter sweep avg should be < 50000ns, was " + avgSweepNs);
    }

    private int populateUniqueArchetypes(int uniqueArchetypes, int entitiesPerArchetype) {
        Set<EntityArchetype> seen = new HashSet<>();
        int tagBits = ArchetypeTagBank.TAGS.length;
        int maxMask = 1 << tagBits;
        int masks = Math.min(uniqueArchetypes, maxMask);

        for (int mask = 0; mask < masks; mask++) {
            for (int n = 0; n < entitiesPerArchetype; n++) {
                EcsEntity entity = ecsWorld.createEntity(ArchetypeBaseEntityFactory.class);
                for (int bit = 0; bit < tagBits; bit++) {
                    if ((mask & (1 << bit)) != 0) {
                        entity.addComponent(ArchetypeTagBank.newTag(bit));
                    }
                }
                seen.add(entity.getArchetype());
            }
        }
        return seen.size();
    }

    private EntityQuery findOrCreateEntityQuery(ComponentFilter filter) throws Exception {
        return (EntityQuery) findOrCreateEntityQueryMethod.invoke(ecsWorld, filter);
    }
}
