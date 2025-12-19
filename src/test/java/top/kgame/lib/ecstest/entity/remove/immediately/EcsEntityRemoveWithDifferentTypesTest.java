package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 移除不同类型的实体测试
 */
public class EcsEntityRemoveWithDifferentTypesTest extends EcsTestBase {
    private EcsEntity entity1;
    private EcsEntity entity12;
    private EcsEntity entity123;
    private EcsAssertions assertions;
    private long removeEntityTime;

    @Test
    void removeDifferentTypesOfEntities() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 创建不同类型的实体
        entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
        entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间移除不同类型的实体
        if (currentTime == removeEntityTime) {
            ecsWorld.requestDestroyEntity(entity1);
            ecsWorld.requestDestroyEntity(entity12);
            ecsWorld.requestDestroyEntity(entity123);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证不同类型的实体在移除前存在，在移除后不存在
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
