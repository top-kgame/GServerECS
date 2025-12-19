package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 实体立即移除后立即添加测试
 */
public class EcsEntityRemoveThenAddTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsEntity newEntity;
    private EcsAssertions assertions;
    private long removeEntityTime;
    private long addEntityTime;

    @Test
    void removeEntityThenAdd() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建实体
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        addEntityTime = interval * 50;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即移除实体
        if (currentTime == removeEntityTime) {
            ecsWorld.requestDestroyEntity(entity);
        }
        
        // 立即添加新实体
        if (currentTime == addEntityTime) {
            newEntity = ecsWorld.createEntity(EntityIndex.E12.getId());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体在移除前存在，在移除后不存在
        if (currentTime >= removeEntityTime && currentTime < addEntityTime) {
            assertions.assertEntityNotExists(entity, currentTime);
        }
        
        // 验证新实体在添加后存在
        if (currentTime >= addEntityTime) {
            assertions.assertEntityExists(newEntity, currentTime);
        }
    }
}
