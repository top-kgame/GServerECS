package top.kgame.lib.ecstest.dispose;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import java.util.Collection;

/**
 * ECS销毁测试用例 - 关闭后的状态验证
 * 测试场景：验证关闭后实体管理器、系统管理器等是否正确清理
 */
class EcsDisposeAfterCloseStateTest extends EcsTestBase {
    private EcsEntity entity1;
    private EcsEntity entity2;
    private EcsEntity entity3;

    @Test
    void verifyStateAfterClose() {
        // 测试场景：验证关闭后的各种状态
        entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        entity2 = ecsWorld.createEntity(EntityIndex.E1.getId());
        entity3 = ecsWorld.createEntity(EntityIndex.E1.getId());
        
        // 记录实体索引
        int index1 = entity1.getIndex();
        int index2 = entity2.getIndex();
        int index3 = entity3.getIndex();
        
        // 更新几次，确保系统运行
        ecsWorld.update(1000);
        ecsWorld.update(2000);
        
        // 验证更新前实体存在
        assert ecsWorld.getEntity(index1) != null : "实体1应该存在";
        assert ecsWorld.getEntity(index2) != null : "实体2应该存在";
        assert ecsWorld.getEntity(index3) != null : "实体3应该存在";
        
        Collection<EcsEntity> allEntitiesBefore = ecsWorld.getAllEntity();
        assert allEntitiesBefore.size() >= 3 : "关闭前应该至少有3个实体";
        
        // 关闭世界
        ecsWorld.close();
        
        // 验证关闭状态
        verifyClosedState(index1, index2, index3);
    }

    @Test
    void verifyStateAfterCloseInUpdate() {
        // 测试场景：在 update 过程中关闭，验证状态
        entity1 = ecsWorld.createEntity(2);
        entity2 = ecsWorld.createEntity(2);
        
        int index1 = entity1.getIndex();
        int index2 = entity2.getIndex();
        
        final int tickInterval = DEFAULT_INTERVAL;
        long endTime = 60000;
        
        updateWorld(0, endTime, tickInterval);
        
        // 验证关闭状态
        assert ecsWorld.isClosed() : "World 应该已关闭";
        verifyEntityCleaned(index1);
        verifyEntityCleaned(index2);
    }

    private void verifyClosedState(int index1, int index2, int index3) {
        // 验证 isClosed() 返回 true
        assert ecsWorld.isClosed() : "isClosed() 应该返回 true";
        
        // 验证所有实体都被清理
        verifyEntityCleaned(index1);
        verifyEntityCleaned(index2);
        verifyEntityCleaned(index3);
        
        // 验证 getAllEntity() 返回空集合
        Collection<EcsEntity> allEntitiesAfter = ecsWorld.getAllEntity();
        assert allEntitiesAfter != null : "getAllEntity() 不应该返回 null";
        assert allEntitiesAfter.isEmpty() : "关闭后 getAllEntity() 应该返回空集合，但发现 " + allEntitiesAfter.size() + " 个实体";
        
        // 验证 currentTime 被重置
        long currentTime = ecsWorld.getCurrentTime();
        assert currentTime == -1 : "关闭后 currentTime 应该被重置为 -1，但实际为 " + currentTime;
    }

    private void verifyEntityCleaned(int entityIndex) {
        EcsEntity found = ecsWorld.getEntity(entityIndex);
        assert found == null : "实体 " + entityIndex + " 应该已被清理，但仍然存在";
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在 update 前关闭
        if (currentTime > 3000 && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 不需要特殊操作
    }
}

