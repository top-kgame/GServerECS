package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 实体立即添加后验证所有组件测试
 */
public class EcsEntityAddVerifyAllComponentsTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long addEntityTime;

    @Test
    void addEntityAndVerifyAllComponents() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即添加实体（包含Component1、Component2、Component3）
        if (currentTime == addEntityTime) {
            entity = ecsWorld.createEntity(EntityIndex.E123.getId());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体存在并包含所有预期的组件
        if (currentTime >= addEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
            
            Component1 comp1 = entity.getComponent(Component1.class);
            Component2 comp2 = entity.getComponent(Component2.class);
            Component3 comp3 = entity.getComponent(Component3.class);
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            
            assertNotNull(comp1, "Entity should contain Component1");
            assertNotNull(comp2, "Entity should contain Component2");
            assertNotNull(comp3, "Entity should contain Component3");
            assertNotNull(lex, "Entity should contain ComponentLexicographic");
        }
    }
}
