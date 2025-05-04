package top.kgame.lib.ecstest.component.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component在系统中立即添加测试用例
 */
class EcsComponentAddInSystemTest extends EcsTestBase {
    private EcsEntity entity;
    private Component2 componentAdd2;
    private long addComponentTime;
    private EcsAssertions assertions;
    private boolean inited = false;
    private Context context = new Context();

    @Test
    void addComponentInSystem() {
        ecsWorld.setContext(context);
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E12.getId());
        componentAdd2 = entity.getComponent(Component2.class);
        Component1 componentAdd1 = entity.getComponent(Component1.class);
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;
        context.addComponentTime = addComponentTime;
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在系统中添加组件，不需要在 beforeUpdate 中处理
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o212");
        } else if (currentTime < addComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        } else if (currentTime == addComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        } else if (currentTime == addComponentTime + interval) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o3123");
        } else {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        }
    }

    public static class Context {
        long addComponentTime;
    }
}

