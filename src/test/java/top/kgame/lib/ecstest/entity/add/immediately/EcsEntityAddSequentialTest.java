package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 实体连续立即添加测试
 */
public class EcsEntityAddSequentialTest extends EcsTestBase {
    private EcsEntity entity1;
    private EcsEntity entity2;
    private EcsEntity entity3;
    private EcsAssertions assertions;
    private long addEntityTime1;
    private long addEntityTime2;
    private long addEntityTime3;

    @Test
    void addEntitiesSequentially() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime1 = interval * 20;
        addEntityTime2 = interval * 21; // 连续添加
        addEntityTime3 = interval * 22; // 连续添加
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 连续立即添加多个实体
        if (currentTime == addEntityTime1) {
            entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        if (currentTime == addEntityTime2) {
            entity2 = ecsWorld.createEntity(EntityIndex.E12.getId());
        }
        if (currentTime == addEntityTime3) {
            entity3 = ecsWorld.createEntity(EntityIndex.E123.getId());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证所有实体都存在
        if (currentTime >= addEntityTime1) {
            assertions.assertEntityExists(entity1, currentTime);
        }
        if (currentTime >= addEntityTime2) {
            assertions.assertEntityExists(entity2, currentTime);
        }
        if (currentTime >= addEntityTime3) {
            assertions.assertEntityExists(entity3, currentTime);
        }
    }
}
