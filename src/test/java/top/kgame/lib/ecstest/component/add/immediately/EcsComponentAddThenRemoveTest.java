package top.kgame.lib.ecstest.component.add.immediately;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component立即添加测试用例 - 添加后立即移除
 */
class EcsComponentAddThenRemoveTest extends EcsTestBase {
    private static final Logger log = LogManager.getLogger(EcsComponentAddThenRemoveTest.class);
    private EcsEntity entity;
    @SuppressWarnings("unused")
    private Component1 componentAdd1;
    @SuppressWarnings("unused")
    private Component2 componentAdd2;
    private long addComponentTime;
    private long removeComponentTime;
    private EcsAssertions assertions;
    private boolean inited = false;

    @Test
    void addThenRemoveComponent() {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E12.getId());
        componentAdd1 = entity.getComponent(Component1.class);
        componentAdd2 = entity.getComponent(Component2.class);
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;
        removeComponentTime = addComponentTime + interval; // 在下一个interval移除组件
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 先添加组件
        if (currentTime == addComponentTime) {
            boolean addResult = entity.addComponent(new Component3());
            assert addResult : "Component add should succeed";
            log.info("Added ComponentAdd3 at time: {}", currentTime);
        }
        
        // 在下一个interval移除组件
        if (currentTime == removeComponentTime) {
            Component3 removed = (Component3) entity.removeComponent(Component3.class);
            assert removed != null : "Component should exist before removal";
            log.info("Removed ComponentAdd3 at time: {}", currentTime);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证添加后立即移除
        if (!inited) {
            inited = true;
            // 初始状态：E12实体，Component1和Component2都已存在
            // Spawn阶段：SystemSpawnDefaultComponent1添加"o1"，SystemSpawnDefaultComponent2添加"o2"
            // Logic阶段：SystemDefaultComponent1添加"1"，SystemDefaultComponent2添加"2"
            // SystemDefaultComponentLexicographic将cache复制到data
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o212");
        } else if (currentTime < addComponentTime) {
            // 在添加组件之前，只有Component1和Component2在更新
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        } else if (currentTime == addComponentTime) {
            // 组件应该被添加
            Component3 comp3 = entity.getComponent(Component3.class);
            assert comp3 != null : "Component3 should be added at time " + currentTime;
            // Spawn阶段：SystemSpawnDefaultComponent3添加"o3"
            // Logic阶段：SystemDefaultComponent1添加"1"，SystemDefaultComponent2添加"2"，SystemDefaultComponent3添加"3"
            // SystemDefaultComponentLexicographic将cache复制到data
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o3123");
        } else if (currentTime == removeComponentTime) {
            // 组件应该被移除
            Component3 comp3 = entity.getComponent(Component3.class);
            assert comp3 == null : "Component3 should be removed at time " + currentTime;
            // 移除后，只有Component1和Component2在更新
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        } else if (currentTime >= removeComponentTime + interval) {
            // 组件应该已被移除，不再存在
            Component3 comp3 = entity.getComponent(Component3.class);
            assert comp3 == null : "Component3 should be removed at time " + currentTime;
            // 验证状态回到移除后的状态，只有Component1和Component2在更新
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        }
    }
}

