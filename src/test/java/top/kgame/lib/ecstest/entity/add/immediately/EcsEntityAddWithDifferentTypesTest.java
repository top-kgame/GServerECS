package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 立即添加不同类型实体测试
 */
public class EcsEntityAddWithDifferentTypesTest extends EcsTestBase {
    private EcsEntity entity1;      // 包含Component1
    private EcsEntity entity12;     // 包含Component1和Component2
    private EcsEntity entity123;    // 包含Component1、Component2和Component3
    private EcsAssertions assertions;
    private long addEntityTime;

    @Test
    void addDifferentTypeEntities() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 同时立即添加不同类型的实体
        if (currentTime == addEntityTime) {
            entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
            entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
            entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证所有实体存在并包含正确的组件
        if (currentTime >= addEntityTime) {
            assertions.assertEntityExists(entity1, currentTime);
            assertions.assertEntityExists(entity12, currentTime);
            assertions.assertEntityExists(entity123, currentTime);
            
            // 验证entity1只包含Component1
            assertNotNull(entity1.getComponent(Component1.class), "entity1 should contain Component1");
            assertNull(entity1.getComponent(Component2.class), "entity1 should not contain Component2");
            assertNull(entity1.getComponent(Component3.class), "entity1 should not contain Component3");
            
            // 验证entity12包含Component1和Component2
            assertNotNull(entity12.getComponent(Component1.class), "entity12 should contain Component1");
            assertNotNull(entity12.getComponent(Component2.class), "entity12 should contain Component2");
            assertNull(entity12.getComponent(Component3.class), "entity12 should not contain Component3");
            
            // 验证entity123包含所有组件
            assertNotNull(entity123.getComponent(Component1.class), "entity123 should contain Component1");
            assertNotNull(entity123.getComponent(Component2.class), "entity123 should contain Component2");
            assertNotNull(entity123.getComponent(Component3.class), "entity123 should contain Component3");
        }
    }
}
