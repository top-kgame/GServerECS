package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 实体立即添加后验证组件测试
 */
public class EcsEntityAddWithComponentVerificationTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long addEntityTime;

    @Test
    void addEntityAndVerifyComponents() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间立即添加实体（包含Component1和Component2）
        if (currentTime == addEntityTime) {
            entity = ecsWorld.createEntity(EntityIndex.E12.getId());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体存在并包含正确的组件
        if (currentTime >= addEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
            Component1 comp1 = entity.getComponent(Component1.class);
            Component2 comp2 = entity.getComponent(Component2.class);
            assertNotNull(comp1, "实体应包含Component1");
            assertNotNull(comp2, "实体应包含Component2");
        }
    }
}
