package top.kgame.lib.ecstest.dispose;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecstest.util.EcsTestBase;

/**
 * ECS销毁测试用例 - 多次调用 close()
 * 测试场景：验证多次调用 close() 方法的安全性
 */
class EcsDisposeMultipleCloseTest extends EcsTestBase {
    private int closeCallCount = 0;

    @Test
    void multipleCloseBeforeUpdate() {
        // 测试场景：在 update 前多次调用 close()
        ecsWorld.createEntity(2);
        
        final int tickInterval = DEFAULT_INTERVAL;
        long endTime = 60000;
        
        updateWorld(0, endTime, tickInterval);
        
        // 断言：多次调用 close() 应该安全，不会抛出异常
        assert closeCallCount >= 1 : "close() 应该至少被调用一次";
        assert ecsWorld.isClosed() : "World 应该已关闭";
    }

    @Test
    void multipleCloseAfterUpdate() {
        // 测试场景：在 update 后多次调用 close()
        ecsWorld.createEntity(2);
        
        final int tickInterval = DEFAULT_INTERVAL;
        long endTime = 60000;
        
        updateWorld(0, endTime, tickInterval);
        
        // 断言：多次调用 close() 应该安全
        assert closeCallCount >= 1 : "close() 应该至少被调用一次";
        assert ecsWorld.isClosed() : "World 应该已关闭";
    }

    @Test
    void multipleCloseAfterAlreadyClosed() {
        // 测试场景：在已关闭状态下多次调用 close()
        ecsWorld.createEntity(2);
        
        // 先关闭一次
        ecsWorld.close();
        assert ecsWorld.isClosed() : "World 应该已关闭";
        
        // 再次调用 close() 多次
        ecsWorld.close();
        ecsWorld.close();
        ecsWorld.close();
        
        // 断言：应该仍然处于关闭状态，不会出错
        assert ecsWorld.isClosed() : "World 应该仍然处于关闭状态";
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在 update 前多次调用 close()
        if (currentTime > 3000 && !ecsWorld.isClosed()) {
            ecsWorld.close();
            closeCallCount++;
            // 再次调用 close()，应该安全
            ecsWorld.close();
            closeCallCount++;
            ecsWorld.close();
            closeCallCount++;
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 在 update 后多次调用 close()
        if (currentTime > 3000 && !ecsWorld.isClosed()) {
            ecsWorld.close();
            closeCallCount++;
            // 再次调用 close()，应该安全
            ecsWorld.close();
            closeCallCount++;
            ecsWorld.close();
            closeCallCount++;
        }
    }
}

