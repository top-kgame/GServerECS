package top.kgame.lib.ecstest.systemgroup;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.core.EcsSystemManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * EcsSystemGroup#addSystem 与 #removeSystem 的测试用例。
 *
 * <p>测试策略：</p>
 * <ul>
 *     <li>使用不存在的包名创建EcsWorld，避免扫描到任何真实/测试System，
 *     使得Group内的子System完全由addSystem/removeSystem控制。</li>
 *     <li>直接调用{@code group.update()}驱动Group对子System的调度，
 *     以隔离验证addSystem/removeSystem本身的行为（不依赖顶层World的调度）。</li>
 *     <li>子System使用{@link CountingSystem}记录各生命周期方法的调用次数进行断言。</li>
 * </ul>
 */
class EcsSystemGroupAddRemoveTest {
    private EcsWorld ecsWorld;
    private EcsSystemManager systemManager;
    private TestSystemGroup group;

    @BeforeEach
    void setUp() {
        // 不存在的包名，保证不会扫描出多余的System
        ecsWorld = EcsWorld.generateInstance("top.kgame.lib.ecs.nonexistent");
        systemManager = new EcsSystemManager(ecsWorld);
        group = new TestSystemGroup();
        group.init(systemManager);
    }

    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    /**
     * 创建并初始化一个子System（init会绑定World与SystemManager）。
     */
    private <T extends CountingSystem> T initChild(T system) {
        system.init(systemManager);
        return system;
    }

    // ==================== addSystem ====================

    @Test
    void addSystem_childParticipatesInUpdate() {
        CountingSystemA a = initChild(new CountingSystemA());

        group.addSystem(a);
        // 仅添加尚未update时不应执行
        assertEquals(0, a.updateCount, "add之后、update之前不应执行子System");
        assertEquals(0, a.startCount, "add之后、update之前不应调用onStart");

        group.update();
        assertEquals(1, a.startCount, "首次update应调用一次onStart");
        assertEquals(1, a.updateCount, "首次update应执行一次子System");

        group.update();
        assertEquals(1, a.startCount, "已启动的子System不应重复调用onStart");
        assertEquals(2, a.updateCount, "再次update应再次执行子System");
    }

    @Test
    void addSystem_multipleChildrenAllParticipate() {
        CountingSystemA a = initChild(new CountingSystemA());
        CountingSystemB b = initChild(new CountingSystemB());
        CountingSystemC c = initChild(new CountingSystemC());

        group.addSystem(a);
        group.addSystem(b);
        group.addSystem(c);

        group.update();

        assertEquals(1, a.updateCount);
        assertEquals(1, b.updateCount);
        assertEquals(1, c.updateCount);
    }

    @Test
    void addSystem_duplicateIsIgnored() {
        CountingSystemA a = initChild(new CountingSystemA());

        group.addSystem(a);
        group.addSystem(a); // 重复添加同一个实例应被忽略

        group.update();

        assertEquals(1, a.updateCount, "重复添加不应导致同一个子System被执行多次");
    }

    // ==================== removeSystem ====================

    @Test
    void removeSystem_isDeferredUntilUpdate() {
        CountingSystemA a = initChild(new CountingSystemA());
        group.addSystem(a);
        group.update();
        assertEquals(1, a.updateCount);

        group.removeSystem(a);
        // removeSystem只是登记待移除，真正的清理发生在下一次update内
        assertEquals(0, a.destroyCount, "removeSystem不应立即销毁子System");
    }

    @Test
    void removeSystem_stopsUpdatesAndCleansUp() {
        CountingSystemA a = initChild(new CountingSystemA());
        group.addSystem(a);

        group.update();
        assertEquals(1, a.updateCount);

        group.removeSystem(a);
        // 移除生效的这次update中：子System会先执行最后一次，随后在本tick末尾被清理
        group.update();
        assertEquals(2, a.updateCount, "移除生效的那次update中子System仍会执行最后一次");
        assertEquals(1, a.stopCount, "清理时应调用一次onStop");
        assertEquals(1, a.destroyCount, "清理时应调用一次onDestroy");

        // 之后不再被调度
        group.update();
        assertEquals(2, a.updateCount, "移除后不应再执行子System");
        assertEquals(1, a.destroyCount, "不应重复销毁");
    }

    @Test
    void removeSystem_onlyRemovesTargetChild() {
        CountingSystemA a = initChild(new CountingSystemA());
        CountingSystemB b = initChild(new CountingSystemB());
        group.addSystem(a);
        group.addSystem(b);

        group.update();
        assertEquals(1, a.updateCount);
        assertEquals(1, b.updateCount);

        group.removeSystem(b);
        group.update();
        assertEquals(2, a.updateCount);
        assertEquals(2, b.updateCount, "被移除的子System在生效tick仍执行最后一次");
        assertEquals(1, b.destroyCount);

        group.update();
        assertEquals(3, a.updateCount, "未被移除的子System应继续被调度");
        assertEquals(2, b.updateCount, "被移除的子System不再执行");
    }

    // ==================== 与Group销毁的交互 ====================

    @Test
    void groupDestroy_cleansAddedChildren() {
        CountingSystemA a = initChild(new CountingSystemA());
        CountingSystemB b = initChild(new CountingSystemB());
        group.addSystem(a);
        group.addSystem(b);
        group.update();

        group.clean();

        assertEquals(1, a.destroyCount, "Group销毁时应清理通过addSystem加入的子System");
        assertEquals(1, b.destroyCount, "Group销毁时应清理通过addSystem加入的子System");
    }

    @Test
    void groupDestroy_flushesPendingRemovals() {
        CountingSystemA a = initChild(new CountingSystemA());
        group.addSystem(a);
        group.update();

        group.removeSystem(a);
        // 尚未update，移除请求仍在待处理队列中
        assertEquals(0, a.destroyCount);

        group.clean();
        assertEquals(1, a.destroyCount, "Group销毁时应处理待移除队列并清理子System");
    }
}
