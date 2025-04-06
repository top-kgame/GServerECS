package top.kgame.lib.ecstest.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.core.EcsSystemManager;
import top.kgame.lib.ecs.core.SystemScheduler;
import top.kgame.lib.ecs.exception.InvalidSystemOrderAnnotation;
import top.kgame.lib.ecstest.core.system.*;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SystemScheduler 全面测试用例
 * 覆盖所有主要功能和边界情况
 */
class SystemSchedulerTest {
    private SystemScheduler scheduler;
    private EcsWorld ecsWorld;
    private EcsSystemManager systemManager;

    @BeforeEach
    void setUp() {
        scheduler = new SystemScheduler();
        // 延迟创建 EcsWorld，只在需要时创建（避免扫描包导致的问题）
        ecsWorld = null;
        systemManager = null;
    }
    
    /**
     * 获取或创建 EcsSystemManager（延迟初始化）
     */
    private EcsSystemManager getSystemManager() {
        if (systemManager == null) {
            // 使用一个不存在的包名，避免扫描到测试系统
            ecsWorld = EcsWorld.generateInstance("top.kgame.lib.ecs.nonexistent");
            systemManager = new EcsSystemManager(ecsWorld);
        }
        return systemManager;
    }

    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    /**
     * 创建系统实例的辅助方法（不初始化）
     */
    private EcsSystem createSystem(Class<? extends EcsSystem> systemClass) {
        try {
            EcsSystem system = systemClass.getConstructor().newInstance();
            // 注意：实际使用中需要调用 system.init(systemManager)
            // 但为了测试 SystemScheduler，我们只需要系统实例
            return system;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create system: " + systemClass.getName(), e);
        }
    }

    /**
     * 创建并初始化系统实例的辅助方法
     */
    private EcsSystem createAndInitSystem(Class<? extends EcsSystem> systemClass) {
        try {
            EcsSystem system = systemClass.getConstructor().newInstance();
            system.init(getSystemManager());
            return system;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create and init system: " + systemClass.getName(), e);
        }
    }

    // ==================== 基本功能测试 ====================

    @Test
    void testAddSystem() {
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        
        scheduler.addSystem(systemA);
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        
        assertEquals(1, sorted.size());
        assertTrue(sorted.contains(systemA));
    }

    @Test
    void testAddMultipleSystems() {
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        TestSystemC systemC = (TestSystemC) createSystem(TestSystemC.class);
        
        scheduler.addSystem(systemA);
        scheduler.addSystem(systemB);
        scheduler.addSystem(systemC);
        
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        assertEquals(3, sorted.size());
    }

    @Test
    void testAddDuplicateSystem() {
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        
        scheduler.addSystem(systemA);
        scheduler.addSystem(systemA); // 重复添加
        
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        assertEquals(1, sorted.size()); // 应该只添加一次
    }

    @Test
    void testRemoveSystem() {
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        
        scheduler.addSystem(systemA);
        scheduler.addSystem(systemB);
        
        scheduler.removeSystem(systemA);

        assertThrows(InvalidSystemOrderAnnotation.class, () -> {
            Collection<EcsSystem> sorted = scheduler.getSortedSystem();
            assertEquals(1, sorted.size());
            assertTrue(sorted.contains(systemB));
            assertFalse(sorted.contains(systemA));
        });
    }

    @Test
    void testRemoveNonExistentSystem() {
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        
        // 移除不存在的系统应该不会抛出异常，只会记录警告
        assertDoesNotThrow(() -> scheduler.removeSystem(systemA));
    }

    @Test
    void testGetSortedSystemEmpty() {
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        assertTrue(sorted.isEmpty());
    }

    @Test
    void testClean() {
        // 使用初始化的系统，因为 clean() 会调用系统的 clean() 方法
        TestSystemA systemA = (TestSystemA) createAndInitSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createAndInitSystem(TestSystemB.class);
        
        scheduler.addSystem(systemA);
        scheduler.addSystem(systemB);
        
        // 验证系统已添加
        assertEquals(2, scheduler.getSortedSystem().size());
        
        // 清理
        scheduler.clean();
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        
        assertTrue(sorted.isEmpty());
    }

    // ==================== 排序功能测试 ====================

    @Test
    void testSimpleDependencyOrder() {
        // A -> B (B在A之后执行)
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        
        scheduler.addSystem(systemB);
        scheduler.addSystem(systemA);
        
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        List<EcsSystem> sortedList = List.copyOf(sorted);
        
        assertEquals(2, sortedList.size());
        assertTrue(sortedList.indexOf(systemA) < sortedList.indexOf(systemB),
                "SystemA should execute before SystemB");
    }

    @Test
    void testBeforeDependencyOrder() {
        // C在B之前执行
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        TestSystemC systemC = (TestSystemC) createSystem(TestSystemC.class);

        scheduler.addSystem(systemA);
        scheduler.addSystem(systemB);
        scheduler.addSystem(systemC);
        
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        List<EcsSystem> sortedList = List.copyOf(sorted);
        
        assertEquals(3, sortedList.size());
        assertTrue(sortedList.indexOf(systemC) < sortedList.indexOf(systemB),
                "SystemC should execute before SystemB");
    }

    @Test
    void testComplexDependencyChain() {
        // 依赖链: A -> D -> B, A -> B, C -> B
        // 期望顺序: A, C, D, B
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        TestSystemC systemC = (TestSystemC) createSystem(TestSystemC.class);
        TestSystemD systemD = (TestSystemD) createSystem(TestSystemD.class);
        
        scheduler.addSystem(systemB);
        scheduler.addSystem(systemD);
        scheduler.addSystem(systemC);
        scheduler.addSystem(systemA);
        
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        List<EcsSystem> sortedList = List.copyOf(sorted);
        
        assertEquals(4, sortedList.size());
        
        int indexA = sortedList.indexOf(systemA);
        int indexB = sortedList.indexOf(systemB);
        int indexC = sortedList.indexOf(systemC);
        int indexD = sortedList.indexOf(systemD);
        
        // A应该在B之前
        assertTrue(indexA < indexB, "A should be before B");
        // C应该在B之前
        assertTrue(indexC < indexB, "C should be before B");
        // D应该在B之前，在A之后
        assertTrue(indexA < indexD, "A should be before D");
        assertTrue(indexD < indexB, "D should be before B");
    }

    @Test
    void testLongDependencyChain() {
        // 长依赖链: A -> B -> E -> F -> G
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        TestSystemD systemD = (TestSystemD) createSystem(TestSystemD.class);
        TestSystemE systemE = (TestSystemE) createSystem(TestSystemE.class);
        TestSystemF systemF = (TestSystemF) createSystem(TestSystemF.class);
        TestSystemG systemG = (TestSystemG) createSystem(TestSystemG.class);
        
        scheduler.addSystem(systemG);
        scheduler.addSystem(systemF);
        scheduler.addSystem(systemE);
        scheduler.addSystem(systemD);
        scheduler.addSystem(systemB);
        scheduler.addSystem(systemA);
        
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        List<EcsSystem> sortedList = List.copyOf(sorted);
        
        assertEquals(6, sortedList.size());
        
        int indexA = sortedList.indexOf(systemA);
        int indexB = sortedList.indexOf(systemB);
        int indexE = sortedList.indexOf(systemE);
        int indexF = sortedList.indexOf(systemF);
        int indexG = sortedList.indexOf(systemG);
        
        assertTrue(indexA < indexB, "A should be before B");
        assertTrue(indexB < indexE, "B should be before E");
        assertTrue(indexE < indexF, "E should be before F");
        assertTrue(indexF < indexG, "F should be before G");
    }

    @Test
    void testLexicographicOrder() {
        // 测试相同依赖计数时按类型名称字典序排序
        TestSystemLexicographic1 system1 = (TestSystemLexicographic1) createSystem(TestSystemLexicographic1.class);
        TestSystemLexicographic2 system2 = (TestSystemLexicographic2) createSystem(TestSystemLexicographic2.class);
        
        scheduler.addSystem(system2);
        scheduler.addSystem(system1);
        
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        List<EcsSystem> sortedList = List.copyOf(sorted);
        
        assertEquals(2, sortedList.size());
        // TestSystemLexicographic1 应该在 TestSystemLexicographic2 之前（字典序）
        assertTrue(sortedList.indexOf(system1) < sortedList.indexOf(system2),
                "Lexicographic1 should be before Lexicographic2 (alphabetical order)");
    }

    @Test
    void testSortStability() {
        // 测试多次调用 getSortedSystem 返回相同的顺序
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        TestSystemC systemC = (TestSystemC) createSystem(TestSystemC.class);
        
        scheduler.addSystem(systemA);
        scheduler.addSystem(systemB);
        scheduler.addSystem(systemC);
        
        Collection<EcsSystem> sorted1 = scheduler.getSortedSystem();
        Collection<EcsSystem> sorted2 = scheduler.getSortedSystem();
        
        List<EcsSystem> list1 = List.copyOf(sorted1);
        List<EcsSystem> list2 = List.copyOf(sorted2);
        
        assertEquals(list1, list2, "Sort order should be stable");
    }

    // ==================== 边界情况测试 ====================

    @Test
    void testSingleSystem() {
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        
        scheduler.addSystem(systemA);
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        
        assertEquals(1, sorted.size());
        assertEquals(systemA, sorted.iterator().next());
    }

    @Test
    void testNoDependencies() {
        // 多个无依赖的系统应该按字典序排序
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemLexicographic1 system1 = (TestSystemLexicographic1) createSystem(TestSystemLexicographic1.class);
        TestSystemLexicographic2 system2 = (TestSystemLexicographic2) createSystem(TestSystemLexicographic2.class);
        
        scheduler.addSystem(system2);
        scheduler.addSystem(system1);
        scheduler.addSystem(systemA);
        
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        assertEquals(3, sorted.size());
    }

    @Test
    void testSelfReference() {
        TestSystemSelfReference system = (TestSystemSelfReference) createSystem(TestSystemSelfReference.class);
        
        scheduler.addSystem(system);

        assertThrows(InvalidSystemOrderAnnotation.class, () -> {
            Collection<EcsSystem> sorted = scheduler.getSortedSystem();
            assertEquals(1, sorted.size());
        });
    }

    @Test
    void testCrossGroupDependency() {
        TestSystemCrossGroup system = (TestSystemCrossGroup) createSystem(TestSystemCrossGroup.class);
        
        scheduler.addSystem(system);

        assertThrows(InvalidSystemOrderAnnotation.class, () -> {
            Collection<EcsSystem> sorted = scheduler.getSortedSystem();
            assertEquals(1, sorted.size());
        });
    }

    @Test
    void testAddRemoveAdd() {
        // 测试添加 -> 移除 -> 再添加
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        
        scheduler.addSystem(systemA);
        assertEquals(1, scheduler.getSortedSystem().size());
        
        scheduler.removeSystem(systemA);
        assertEquals(0, scheduler.getSortedSystem().size());
        
        scheduler.addSystem(systemA);
        assertEquals(1, scheduler.getSortedSystem().size());
    }

    @Test
    void testMultipleAddRemove() {
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        TestSystemC systemC = (TestSystemC) createSystem(TestSystemC.class);
        
        scheduler.addSystem(systemA);
        scheduler.addSystem(systemB);
        scheduler.addSystem(systemC);
        assertEquals(3, scheduler.getSortedSystem().size());
        
        scheduler.removeSystem(systemB);
        assertThrows(InvalidSystemOrderAnnotation.class, () -> scheduler.getSortedSystem().size());
    }

    @Test
    void testSortAfterAddSystem() {
        // 测试：排序之后，添加system，再次排序
        // 初始系统：A -> B (B在A之后执行)
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        
        scheduler.addSystem(systemA);
        scheduler.addSystem(systemB);
        
        // 第一次排序并验证
        Collection<EcsSystem> sorted1 = scheduler.getSortedSystem();
        List<EcsSystem> sortedList1 = List.copyOf(sorted1);
        assertEquals(2, sortedList1.size());
        int indexA1 = sortedList1.indexOf(systemA);
        int indexB1 = sortedList1.indexOf(systemB);
        assertTrue(indexA1 < indexB1, "A should be before B in first sort");
        
        // 添加新系统：C在B之前执行
        TestSystemC systemC = (TestSystemC) createSystem(TestSystemC.class);
        scheduler.addSystem(systemC);
        
        // 再次排序并验证
        Collection<EcsSystem> sorted2 = scheduler.getSortedSystem();
        List<EcsSystem> sortedList2 = List.copyOf(sorted2);
        assertEquals(3, sortedList2.size());
        
        int indexA2 = sortedList2.indexOf(systemA);
        int indexB2 = sortedList2.indexOf(systemB);
        int indexC2 = sortedList2.indexOf(systemC);
        
        // 验证依赖关系：A在B之前，C在B之前
        assertTrue(indexA2 < indexB2, "A should be before B after adding C");
        assertTrue(indexC2 < indexB2, "C should be before B after adding C");
        
        // 添加另一个系统：D在A之后、B之前执行
        TestSystemD systemD = (TestSystemD) createSystem(TestSystemD.class);
        scheduler.addSystem(systemD);
        
        // 第三次排序并验证
        Collection<EcsSystem> sorted3 = scheduler.getSortedSystem();
        List<EcsSystem> sortedList3 = List.copyOf(sorted3);
        assertEquals(4, sortedList3.size());
        
        int indexA3 = sortedList3.indexOf(systemA);
        int indexB3 = sortedList3.indexOf(systemB);
        int indexC3 = sortedList3.indexOf(systemC);
        int indexD3 = sortedList3.indexOf(systemD);
        
        // 验证依赖关系：A -> D -> B, C -> B
        assertTrue(indexA3 < indexD3, "A should be before D");
        assertTrue(indexD3 < indexB3, "D should be before B");
        assertTrue(indexC3 < indexB3, "C should be before B");
    }

    @Test
    void testSortAfterRemoveSystem() {
        // 测试：排序之后，删除system，再次排序
        // 初始系统：A -> D -> B, C -> B
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        TestSystemC systemC = (TestSystemC) createSystem(TestSystemC.class);
        TestSystemD systemD = (TestSystemD) createSystem(TestSystemD.class);
        
        scheduler.addSystem(systemA);
        scheduler.addSystem(systemB);
        scheduler.addSystem(systemC);
        scheduler.addSystem(systemD);
        
        // 第一次排序并验证
        Collection<EcsSystem> sorted1 = scheduler.getSortedSystem();
        List<EcsSystem> sortedList1 = List.copyOf(sorted1);
        assertEquals(4, sortedList1.size());
        
        int indexA1 = sortedList1.indexOf(systemA);
        int indexB1 = sortedList1.indexOf(systemB);
        int indexC1 = sortedList1.indexOf(systemC);
        int indexD1 = sortedList1.indexOf(systemD);
        
        // 验证初始依赖关系
        assertTrue(indexA1 < indexD1, "A should be before D");
        assertTrue(indexD1 < indexB1, "D should be before B");
        assertTrue(indexC1 < indexB1, "C should be before B");
        
        // 删除系统D
        scheduler.removeSystem(systemD);
        
        // 再次排序并验证
        Collection<EcsSystem> sorted2 = scheduler.getSortedSystem();
        List<EcsSystem> sortedList2 = List.copyOf(sorted2);
        assertEquals(3, sortedList2.size());
        
        assertFalse(sortedList2.contains(systemD), "D should be removed");
        
        int indexA2 = sortedList2.indexOf(systemA);
        int indexB2 = sortedList2.indexOf(systemB);
        int indexC2 = sortedList2.indexOf(systemC);
        
        // 验证删除D后的依赖关系：A -> B, C -> B
        assertTrue(indexA2 < indexB2, "A should be before B after removing D");
        assertTrue(indexC2 < indexB2, "C should be before B after removing D");
        
        // 删除系统C
        scheduler.removeSystem(systemC);
        
        // 第三次排序并验证
        Collection<EcsSystem> sorted3 = scheduler.getSortedSystem();
        List<EcsSystem> sortedList3 = List.copyOf(sorted3);
        assertEquals(2, sortedList3.size());
        
        assertFalse(sortedList3.contains(systemC), "C should be removed");
        
        int indexA3 = sortedList3.indexOf(systemA);
        int indexB3 = sortedList3.indexOf(systemB);
        
        // 验证删除C后的依赖关系：A -> B
        assertTrue(indexA3 < indexB3, "A should be before B after removing C");
    }

    // ==================== 错误情况测试 ====================

    @Test
    void testCircularDependency() {
        // 测试环形依赖应该抛出异常
        TestSystemCircular1 system1 = (TestSystemCircular1) createSystem(TestSystemCircular1.class);
        TestSystemCircular2 system2 = (TestSystemCircular2) createSystem(TestSystemCircular2.class);
        
        scheduler.addSystem(system1);
        scheduler.addSystem(system2);
        
        // 环形依赖应该导致异常
        assertThrows(RuntimeException.class, () -> {
            scheduler.getSortedSystem();
        }, "Circular dependency should throw RuntimeException");
    }

    // ==================== 综合测试 ====================

    @Test
    void testComplexScenario() {
        // 综合测试：包含多种依赖关系的复杂场景
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        TestSystemB systemB = (TestSystemB) createSystem(TestSystemB.class);
        TestSystemC systemC = (TestSystemC) createSystem(TestSystemC.class);
        TestSystemD systemD = (TestSystemD) createSystem(TestSystemD.class);
        TestSystemLexicographic1 system1 = (TestSystemLexicographic1) createSystem(TestSystemLexicographic1.class);
        
        // 乱序添加
        scheduler.addSystem(systemD);
        scheduler.addSystem(systemB);
        scheduler.addSystem(system1);
        scheduler.addSystem(systemC);
        scheduler.addSystem(systemA);
        
        Collection<EcsSystem> sorted = scheduler.getSortedSystem();
        List<EcsSystem> sortedList = List.copyOf(sorted);
        
        assertEquals(5, sortedList.size());
        
        // 验证关键依赖关系
        int indexA = sortedList.indexOf(systemA);
        int indexB = sortedList.indexOf(systemB);
        int indexC = sortedList.indexOf(systemC);
        int indexD = sortedList.indexOf(systemD);
        
        assertTrue(indexA < indexB, "A should be before B");
        assertTrue(indexC < indexB, "C should be before B");
        assertTrue(indexA < indexD, "A should be before D");
        assertTrue(indexD < indexB, "D should be before B");
    }

    @Test
    void testToString() {
        TestSystemA systemA = (TestSystemA) createSystem(TestSystemA.class);
        scheduler.addSystem(systemA);
        
        String str = scheduler.toString();
        assertNotNull(str);
        assertTrue(str.contains("SystemScheduler") || str.contains("SortableSystemList"));
    }
}

