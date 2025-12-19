package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 实体立即移除后再次销毁测试
 * 注意：移除实体就是销毁，再次销毁应该被忽略
 */
public class EcsEntityRemoveThenDestroyTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long removeEntityTime;
    private long destroyEntityTime;

    @Test
    void removeEntityThenDestroy() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建实体
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        destroyEntityTime = interval * 50;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即移除实体
        if (currentTime == removeEntityTime) {
            ecsWorld.requestDestroyEntity(entity);
        }
        
        // 尝试再次销毁已移除的实体（应该被忽略）
        if (currentTime == destroyEntityTime) {
            ecsWorld.requestDestroyEntity(entity);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体在移除后不存在
        if (currentTime >= removeEntityTime) {
            assertions.assertEntityNotExists(entity, currentTime);
        }
    }
}
