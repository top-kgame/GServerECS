package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 不同时间点添加多个实体测试
 */
public class EcsEntityAddMultipleAtDifferentTimesTest extends EcsTestBase {
    private EcsEntity entity1;
    private EcsEntity entity12;
    private EcsEntity entity123;
    private EcsAssertions assertions;
    private long addEntityTime1;
    private long addEntityTime12;
    private long addEntityTime123;

    @Test
    void addMultipleEntitiesAtDifferentTimes() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime1 = interval * 20;
        addEntityTime12 = interval * 40;
        addEntityTime123 = interval * 60;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在不同时间点立即添加实体
        if (currentTime == addEntityTime1) {
            entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        if (currentTime == addEntityTime12) {
            entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
        }
        if (currentTime == addEntityTime123) {
            entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证每个实体在添加后存在
        if (currentTime >= addEntityTime1) {
            assertions.assertEntityExists(entity1, currentTime);
        }
        if (currentTime >= addEntityTime12) {
            assertions.assertEntityExists(entity12, currentTime);
        }
        if (currentTime >= addEntityTime123) {
            assertions.assertEntityExists(entity123, currentTime);
        }
    }
}
