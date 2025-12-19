package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 单个实体立即移除测试 - 在afterUpdate中移除
 */
public class EcsEntityRemoveSingleAfterUpdateTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long removeEntityTime;
    private boolean entityRemoved = false;

    @Test
    void removeSingleEntityAfterUpdate() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建实体
        entity = ecsWorld.createEntity(EntityIndex.E12.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 在指定时间立即移除实体
        if (currentTime == removeEntityTime && !entityRemoved) {
            ecsWorld.requestDestroyEntity(entity);
            entityRemoved = true;
        }
        
        // 验证实体在移除前存在，在移除后不存在
        if (currentTime < removeEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
        }
        if (currentTime > removeEntityTime && entityRemoved) {
            assertions.assertEntityNotExists(entity, currentTime);
        }
    }
}
