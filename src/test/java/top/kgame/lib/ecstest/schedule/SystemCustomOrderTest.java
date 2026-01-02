package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * System自定义执行顺序测试
 * 测试通过@Before和@After注解定义的系统执行顺序
 */
class SystemCustomOrderTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private ComponentLexicographic componentLexicographic;

    @Test
    void testCustomSystemOrder() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 创建实体，包含ComponentLexicographic组件
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        
        // 执行更新循环
        // SystemOrderTestA -> SystemOrderTestC -> SystemOrderTestB
        // 因为：A无依赖，C在B之前，B在A之后
        updateWorld(0, DEFAULT_INTERVAL * 10, DEFAULT_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (currentTime == 0) {
            // 第一次更新：验证执行顺序为 A -> C -> B
            // 因为A无依赖最先执行，C在B之前，B在A之后
            assert componentLexicographic.data.contains("ACB");
        } else {
            // 后续更新：每次都是ACB
            assert componentLexicographic.data.contains("ACB");
        }
    }
}
