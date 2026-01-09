package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Entity移除对System执行顺序影响的测试 - 立即模式
 */
class SystemEntityRemoveOrderImmediatelyTest extends EcsTestBase {
    private EcsEntity removeEntity;
    private ComponentLexicographic componentLexicographic;
    private long modifyTime;

    @Test
    void testImmediatelyRemoveEntity() {
        EcsEntity entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        modifyTime = DEFAULT_INTERVAL * 10;
        removeEntity = ecsWorld.createEntity(EntityIndex.E1.getId());
        updateWorld(0, DEFAULT_INTERVAL * 20, DEFAULT_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        if (currentTime == modifyTime) {
            ecsWorld.requestDestroyEntity(removeEntity);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        assertTrue(componentLexicographic.data.contains("ACBDE"),
                "system的执行顺序不应受requestDestroyEntity的影响");
    }
}

