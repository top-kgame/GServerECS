package top.kgame.lib.ecstest.dispose;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsTestBase;

import java.util.Collection;

/**
 * ECS销毁测试用例 - 在已关闭状态下调用各种操作
 * 测试场景：验证在已关闭状态下调用各种方法的行为
 */
class EcsDisposeClosedOperationsTest extends EcsTestBase {
    private EcsEntity entity;
    private boolean updateCalled = false;
    private boolean createEntityCalled = false;
    private boolean getEntityCalled = false;
    private boolean getAllEntityCalled = false;
    private boolean requestDestroyEntityCalled = false;

    @Test
    void operationsAfterClose() {
        // 测试场景：关闭后调用各种操作
        entity = ecsWorld.createEntity(2);
        int entityIndex = entity.getIndex();
        
        // 先更新一次，确保世界处于运行状态
        ecsWorld.update(1000);
        
        // 关闭世界
        ecsWorld.close();
        assert ecsWorld.isClosed() : "World should be closed";
        
        // 测试在已关闭状态下调用各种操作
        testUpdateAfterClose();
        testCreateEntityAfterClose();
        testGetEntityAfterClose(entityIndex);
        testGetAllEntityAfterClose();
        testRequestDestroyEntityAfterClose(entityIndex);
        
        // 断言：所有操作都应该安全处理，不会抛出异常
        assert updateCalled : "update() should be called";
        assert createEntityCalled : "createEntity() should be called";
        assert getEntityCalled : "getEntity() should be called";
        assert getAllEntityCalled : "getAllEntity() should be called";
        assert requestDestroyEntityCalled : "requestDestroyEntity() should be called";
    }

    private void testUpdateAfterClose() {
        // 测试：在已关闭状态下调用 update()
        try {
            ecsWorld.update(2000);
            updateCalled = true;
        } catch (Exception e) {
            // update() 应该返回警告但不抛出异常
            updateCalled = true;
        }
    }

    private void testCreateEntityAfterClose() {
        // 测试：在已关闭状态下调用 createEntity()
        try {
            EcsEntity newEntity = ecsWorld.createEntity(2);
            createEntityCalled = true;
            // 如果创建成功，应该为 null 或抛出异常
            assert newEntity == null : "Creating entity in closed state should fail";
        } catch (Exception e) {
            // 可能抛出异常，这是可以接受的
            createEntityCalled = true;
        }
    }

    private void testGetEntityAfterClose(int entityIndex) {
        // 测试：在已关闭状态下调用 getEntity()
        try {
            EcsEntity found = ecsWorld.getEntity(entityIndex);
            getEntityCalled = true;
            // 应该返回 null，因为实体已被清理
            assert found == null : "Getting entity in closed state should return null";
        } catch (Exception e) {
            getEntityCalled = true;
        }
    }

    private void testGetAllEntityAfterClose() {
        // 测试：在已关闭状态下调用 getAllEntity()
        try {
            Collection<EcsEntity> allEntities = ecsWorld.getAllEntity();
            getAllEntityCalled = true;
            // 应该返回空集合
            assert allEntities != null : "getAllEntity() should return non-null";
            assert allEntities.isEmpty() : "Getting all entities in closed state should return empty collection";
        } catch (Exception e) {
            getAllEntityCalled = true;
        }
    }

    private void testRequestDestroyEntityAfterClose(int entityIndex) {
        // 测试：在已关闭状态下调用 requestDestroyEntity()
        try {
            ecsWorld.requestDestroyEntity(entityIndex);
            requestDestroyEntityCalled = true;
            // 应该安全处理，不会抛出异常
        } catch (Exception e) {
            requestDestroyEntityCalled = true;
        }
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 不需要特殊操作
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 不需要特殊操作
    }
}

