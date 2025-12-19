package top.kgame.lib.ecstest.entity.remove.immediately;

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
 * 实体立即移除时验证所有组件测试
 */
public class EcsEntityRemoveVerifyAllComponentsTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long removeEntityTime;

    @Test
    void removeEntityAndVerifyAllComponents() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 创建包含多个组件的实体
        entity = ecsWorld.createEntity(EntityIndex.E123.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在移除前验证所有组件存在
        if (currentTime == removeEntityTime - interval) {
            Component1 comp1 = entity.getComponent(Component1.class);
            Component2 comp2 = entity.getComponent(Component2.class);
            Component3 comp3 = entity.getComponent(Component3.class);
            assertNotNull(comp1, "Component1 should exist before remove");
            assertNotNull(comp2, "Component2 should exist before remove");
            assertNotNull(comp3, "Component3 should exist before remove");
        }
        
        // 立即移除实体
        if (currentTime == removeEntityTime) {
            ecsWorld.requestDestroyEntity(entity);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体在移除前存在，在移除后不存在
        if (currentTime < removeEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
        }
        if (currentTime >= removeEntityTime) {
            assertions.assertEntityNotExists(entity, currentTime);
            // 实体移除后，所有组件也应该被清理
            EcsEntity found = ecsWorld.getEntity(entity.getIndex());
            assertNull(found, "Entity should be null after remove");
        }
    }
}
