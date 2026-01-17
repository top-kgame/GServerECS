package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 实体立即添加后立即添加组件测试
 */
public class EcsEntityAddThenAddComponentTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long addEntityTime;
    private long addComponentTime;

    @Test
    void addEntityThenAddComponent() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        addComponentTime = interval * 35;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即添加实体
        if (currentTime == addEntityTime) {
            entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        }
        
        // 在实体添加后立即添加组件
        if (currentTime == addComponentTime && entity != null) {
            entity.addComponent(new Component3());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体存在
        if (currentTime >= addEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
        }
        
        // 验证组件已添加
        if (currentTime >= addComponentTime && entity != null) {
            Component3 comp3 = entity.getComponent(Component3.class);
            assertNotNull(comp3, "Entity should contain Component3");
        }
    }
}
