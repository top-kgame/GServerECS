package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Entity添加对System执行顺序影响的测试 - 立即模式
 */
class SystemEntityAddOrderImmediatelyTest extends EcsTestBase {
    private EcsEntity entity;
    private ComponentLexicographic componentLexicographic;
    private long modifyTime;

    @Test
    void testImmediatelyAddEntity() {
        modifyTime = DEFAULT_INTERVAL * 10;
        entity = ecsWorld.createEntity(EntityIndex.E12.getId());
        componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        updateWorld(0, DEFAULT_INTERVAL * 20, DEFAULT_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        if (currentTime == modifyTime && entity == null) {
            ecsWorld.createEntity(EntityIndex.E12.getId());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (currentTime < modifyTime) {
            return;
        }
        if (componentLexicographic == null) {
            return;
        }
        // SystemDefaultComponent1应该执行，记录"1"（可能落在data或cache）
        assertTrue(componentLexicographic.data.contains("ACBDE"),
            "system的执行顺序不应受createEntity影响");
    }
}

