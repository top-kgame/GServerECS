package top.kgame.lib.ecstest.component.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component在更新前立即添加测试用例
 */
class EcsComponentAddBeforeUpdateTest extends EcsTestBase {
    private EcsEntity entity;
    private long addComponentTime;
    private EcsAssertions assertions;
    private boolean inited = false;

    @Test
    void addComponentBeforeUpdate() {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在更新前添加组件
        if (currentTime == addComponentTime) {
            EcsEntity entity1 = ecsWorld.getEntity(entity.getIndex());
            assert entity1 == entity;
            entity1.addComponent(new Component2());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o11");
        } else if (currentTime < addComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1");
        } else if (currentTime == addComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o212");
        } else {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        }
    }
}

