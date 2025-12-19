package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 多个实体立即移除测试 - 在beforeUpdate中同时移除多个实体
 */
public class EcsEntityRemoveMultipleBeforeUpdateTest extends EcsTestBase {
    private EcsEntity entity1;
    private EcsEntity entity12;
    private EcsEntity entity123;
    private EcsAssertions assertions;
    private long removeEntityTime;

    @Test
    void removeMultipleEntitiesBeforeUpdate() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建多个实体
        entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
        entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间同时移除多个实体
        if (currentTime == removeEntityTime) {
            ecsWorld.requestDestroyEntity(entity1);
            ecsWorld.requestDestroyEntity(entity12);
            ecsWorld.requestDestroyEntity(entity123);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证多个实体在移除前存在，在移除后不存在
        if (currentTime < removeEntityTime) {
            assertions.assertEntityExists(entity1, currentTime);
            assertions.assertEntityExists(entity12, currentTime);
            assertions.assertEntityExists(entity123, currentTime);
        }
        if (currentTime >= removeEntityTime) {
            assertions.assertEntityNotExists(entity1, currentTime);
            assertions.assertEntityNotExists(entity12, currentTime);
            assertions.assertEntityNotExists(entity123, currentTime);
        }
    }
}
