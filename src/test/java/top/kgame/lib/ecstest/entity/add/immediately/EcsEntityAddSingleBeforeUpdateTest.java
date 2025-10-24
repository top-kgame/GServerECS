package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 单个实体立即添加测试 - 在beforeUpdate中添加
 */
public class EcsEntityAddSingleBeforeUpdateTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long addEntityTime;

    @Test
    void addSingleEntityBeforeUpdate() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间立即添加实体
        if (currentTime == addEntityTime) {
            entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体在添加后存在
        if (currentTime >= addEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
        }
    }
}
