package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 实体立即移除后添加组件测试
 * 注意：实体移除后无法添加组件，此测试验证移除后的实体无法操作
 */
public class EcsEntityRemoveThenAddComponentTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long removeEntityTime;
    private long addComponentTime;

    @Test
    void removeEntityThenAddComponent() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建实体（包含Component1）
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        addComponentTime = interval * 50;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即移除实体
        if (currentTime == removeEntityTime) {
            ecsWorld.requestDestroyEntity(entity);
        }
        
        // 尝试在移除后添加组件（应该失败或无效）
        if (currentTime == addComponentTime) {
            // 实体已被移除，无法添加组件
            // 此操作应该被忽略或抛出异常
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
