package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsStandaloneUpdateSystem;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EcsStandaloneUpdateSystem测试用例
 * 测试单例更新系统的功能
 * Standalone系统不绑定实体，总是会执行，但不会处理实体
 */
class SystemEcsStandaloneUpdateSystemTest extends EcsTestBase {
    private static String standaloneSystemMarker = "";

    @Test
    void testEcsStandaloneUpdateSystem() {
        // 重置标记
        standaloneSystemMarker = "";

        var entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        var entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
        var entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());

        updateWorld(0, DEFAULT_INTERVAL * 5, DEFAULT_INTERVAL);
        
        // 验证Standalone系统执行了（通过标记）
        assertFalse(standaloneSystemMarker.isEmpty(), "Standalone系统应已执行");
        assertTrue(standaloneSystemMarker.contains("StandaloneSystem"), "Standalone系统标记应存在");

    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证ECS世界正常运行
        assertNotNull(ecsWorld, "ECS世界应存在");
        assertFalse(ecsWorld.isClosed(), "ECS世界不应被关闭");
    }

    /**
     * 测试用的Standalone系统
     */
    public static class TestStandaloneSystem extends EcsStandaloneUpdateSystem {
        @Override
        protected void update() {
            // 标记系统已执行
            standaloneSystemMarker += "StandaloneSystem";
        }
    }
}
