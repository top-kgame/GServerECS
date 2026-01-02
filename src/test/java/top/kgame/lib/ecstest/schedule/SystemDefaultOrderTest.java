package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * System默认执行顺序测试
 * 测试无注解时系统按字典序执行
 */
class SystemDefaultOrderTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private ComponentLexicographic componentLexicographic;

    @Test
    void testDefaultSystemOrder() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 创建实体，包含ComponentLexicographic组件
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        
        // 执行更新循环
        // SystemOrderTestD和SystemOrderTestE都无注解，应该按字典序执行：D -> E
        updateWorld(0, DEFAULT_INTERVAL * 10, DEFAULT_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (currentTime == 0) {
            // 第一次更新：验证执行顺序为 D -> E（字典序）
            assert componentLexicographic.data.contains("DE");
        } else {
            // 后续更新：每次都是DE
            assert componentLexicographic.data.contains("DE");
        }
    }
}
