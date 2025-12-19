package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 多个实体在不同时间立即移除测试
 */
public class EcsEntityRemoveMultipleAtDifferentTimesTest extends EcsTestBase {
    private EcsEntity entity1;
    private EcsEntity entity12;
    private EcsEntity entity123;
    private EcsAssertions assertions;
    private long removeEntityTime1;
    private long removeEntityTime12;
    private long removeEntityTime123;

    @Test
    void removeMultipleEntitiesAtDifferentTimes() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建多个实体
        entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
        entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime1 = interval * 30;
        removeEntityTime12 = interval * 40;
        removeEntityTime123 = interval * 50;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在不同时间移除不同的实体
        if (currentTime == removeEntityTime1) {
            ecsWorld.requestDestroyEntity(entity1);
        }
        if (currentTime == removeEntityTime12) {
            ecsWorld.requestDestroyEntity(entity12);
        }
        if (currentTime == removeEntityTime123) {
            ecsWorld.requestDestroyEntity(entity123);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证每个实体在移除前存在，在移除后不存在
        if (currentTime < removeEntityTime1) {
            assertions.assertEntityExists(entity1, currentTime);
        }
        if (currentTime >= removeEntityTime1) {
            assertions.assertEntityNotExists(entity1, currentTime);
        }
        
        if (currentTime < removeEntityTime12) {
            assertions.assertEntityExists(entity12, currentTime);
        }
        if (currentTime >= removeEntityTime12) {
            assertions.assertEntityNotExists(entity12, currentTime);
        }
        
        if (currentTime < removeEntityTime123) {
            assertions.assertEntityExists(entity123, currentTime);
        }
        if (currentTime >= removeEntityTime123) {
            assertions.assertEntityNotExists(entity123, currentTime);
        }
    }
}
