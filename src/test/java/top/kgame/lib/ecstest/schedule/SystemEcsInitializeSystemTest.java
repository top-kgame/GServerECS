package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EcsInitializeSystem测试用例
 * 测试实体初始化系统的功能
 */
class SystemEcsInitializeSystemTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;

    @Test
    void testEcsInitializeSystem() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 创建实体（包含Component1，会被SystemSpawnDefaultComponent1处理）
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
        assertNotNull(lex, "实体应包含ComponentLexicographic组件");
        
        updateWorld(0, DEFAULT_INTERVAL * 5, DEFAULT_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (entity != null) {
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            if (lex != null) {
                // 验证初始化系统已执行（cache应包含"o1"，表示SystemSpawnDefaultComponent1已执行）
                if (currentTime == 0) {
                    assertTrue(lex.data.contains("o1"),
                        "初始化系统应在第一次更新时执行，cache应包含'o1'");
                }
            }
        }
    }
}
