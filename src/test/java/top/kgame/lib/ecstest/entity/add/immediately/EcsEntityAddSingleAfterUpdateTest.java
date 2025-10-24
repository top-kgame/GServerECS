package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 单个实体立即添加测试 - 在afterUpdate中添加
 */
public class EcsEntityAddSingleAfterUpdateTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long addEntityTime;
    private boolean entityAdded = false;

    @Test
    void addSingleEntityAfterUpdate() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 在指定时间立即添加实体
        if (currentTime == addEntityTime && !entityAdded) {
            entity = ecsWorld.createEntity(EntityIndex.E12.getId());
            entityAdded = true;
        }
        
        // 验证实体在添加后存在
        if (currentTime >= addEntityTime && entityAdded) {
            assertions.assertEntityExists(entity, currentTime);
        }
    }
}
