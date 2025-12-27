package top.kgame.lib.ecstest.system;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.extensions.component.DestroyingComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.Component4;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 预制System测试用例
 * 测试top.kgame.lib.ecs.extensions.system包中所有的预制system
 */
class SystemBuiltInSystemsTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;

    @Test
    void testEcsOneComponentUpdateSystem() {
        // EcsOneComponentUpdateSystem已经在其他测试中使用，这里验证基本功能
        assertions = new EcsAssertions(ecsWorld);
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
                // 验证系统已执行（cache被更新）
                assertNotEquals(lex.data, "","系统应已更新ComponentLexicographic");
            }
        }
    }
}
