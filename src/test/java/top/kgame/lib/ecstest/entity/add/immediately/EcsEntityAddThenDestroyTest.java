package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 实体立即添加后立即销毁测试
 */
public class EcsEntityAddThenDestroyTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long addEntityTime;
    private long destroyEntityTime;

    @Test
    void addEntityThenDestroy() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        destroyEntityTime = interval * 50;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即添加实体
        if (currentTime == addEntityTime) {
            entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        
        // 立即销毁实体
        if (currentTime == destroyEntityTime && entity != null) {
            ecsWorld.requestDestroyEntity(entity);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体在添加后存在，在销毁后不存在
        if (currentTime >= addEntityTime && currentTime < destroyEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
        }
        if (currentTime >= destroyEntityTime) {
            assertions.assertEntityNotExists(entity, currentTime);
        }
    }
}
