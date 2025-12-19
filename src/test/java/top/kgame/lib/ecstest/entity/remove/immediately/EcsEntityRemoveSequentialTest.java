package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 实体连续立即移除测试
 */
public class EcsEntityRemoveSequentialTest extends EcsTestBase {
    private EcsEntity entity1;
    private EcsEntity entity2;
    private EcsEntity entity3;
    private EcsAssertions assertions;
    private long removeEntityTime1;
    private long removeEntityTime2;
    private long removeEntityTime3;

    @Test
    void removeEntitiesSequentially() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建多个实体
        entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        entity2 = ecsWorld.createEntity(EntityIndex.E12.getId());
        entity3 = ecsWorld.createEntity(EntityIndex.E123.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime1 = interval * 20;
        removeEntityTime2 = interval * 21; // 连续移除
        removeEntityTime3 = interval * 22; // 连续移除
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 连续立即移除多个实体
        if (currentTime == removeEntityTime1) {
            ecsWorld.requestDestroyEntity(entity1);
        }
        if (currentTime == removeEntityTime2) {
            ecsWorld.requestDestroyEntity(entity2);
        }
        if (currentTime == removeEntityTime3) {
            ecsWorld.requestDestroyEntity(entity3);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证每个实体在移除前存在，在移除后不存在
        if (currentTime < removeEntityTime1) {
            assertions.assertEntityExists(entity1, currentTime);
            assertions.assertEntityExists(entity2, currentTime);
            assertions.assertEntityExists(entity3, currentTime);
        }
        if (currentTime >= removeEntityTime1) {
            assertions.assertEntityNotExists(entity1, currentTime);
        }
        if (currentTime >= removeEntityTime2) {
            assertions.assertEntityNotExists(entity2, currentTime);
        }
        if (currentTime >= removeEntityTime3) {
            assertions.assertEntityNotExists(entity3, currentTime);
        }
    }
}
