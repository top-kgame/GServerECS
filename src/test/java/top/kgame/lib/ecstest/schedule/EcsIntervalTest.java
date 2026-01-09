package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System间隔测试用例
 */
public class EcsIntervalTest extends EcsTestBase {
    private EcsEntity entity;
    private static final int TICK_INTERVAL = DEFAULT_INTERVAL;
    public static final int TEST_TICK_INTERVAL  = TICK_INTERVAL * 2;
    private ComponentLexicographic componentLexicographic;

    @Test
    void updateWorld() {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        
        // 执行更新循环
        updateWorld(0, 6000, TICK_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (currentTime == 0) {
            assertTrue(componentLexicographic.data.contains("interval"), "第一帧的执行不应不受tickRate的影响");
            return;
        }
        if (currentTime % TEST_TICK_INTERVAL == 0) {
            assertTrue(componentLexicographic.data.contains("interval"), "tickRate没有生效");
        } else if (currentTime % TICK_INTERVAL == 0) {
            assertFalse(componentLexicographic.data.contains("interval"), "tickRate没有生效");
        }
    }
} 