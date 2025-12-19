package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 单个实体立即移除测试 - 在beforeUpdate中移除
 */
public class EcsEntityRemoveSingleBeforeUpdateTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long removeEntityTime;

    @Test
    void removeSingleEntityBeforeUpdate() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建实体
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间立即移除实体
        if (currentTime == removeEntityTime) {
            ecsWorld.requestDestroyEntity(entity);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体在移除前存在，在移除后不存在
        if (currentTime < removeEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
        }
        if (currentTime >= removeEntityTime) {
            assertions.assertEntityNotExists(entity, currentTime);
        }
    }
}
