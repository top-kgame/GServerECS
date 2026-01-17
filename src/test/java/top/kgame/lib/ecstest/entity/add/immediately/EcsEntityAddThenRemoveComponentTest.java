package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 实体立即添加后立即移除组件测试
 */
public class EcsEntityAddThenRemoveComponentTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long addEntityTime;
    private long removeComponentTime;

    @Test
    void addEntityThenRemoveComponent() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        removeComponentTime = interval * 35;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即添加实体（包含Component1）
        if (currentTime == addEntityTime) {
            entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        
        // 在实体添加后立即移除组件
        if (currentTime == removeComponentTime && entity != null) {
            entity.removeComponent(Component1.class);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体存在
        if (currentTime >= addEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
        }
        
        // 验证组件已移除
        if (currentTime >= removeComponentTime && entity != null) {
            Component1 comp1 = entity.getComponent(Component1.class);
            assertNull(comp1, "Entity should not contain Component1");
        }
    }
}
