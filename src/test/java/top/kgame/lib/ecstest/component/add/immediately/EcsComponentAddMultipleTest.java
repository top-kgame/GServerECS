package top.kgame.lib.ecstest.component.add.immediately;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.*;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component立即添加测试用例 - 多个组件连续添加
 */
class EcsComponentAddMultipleTest extends EcsTestBase {
    private static final Logger log = LogManager.getLogger(EcsComponentAddMultipleTest.class);
    private EcsEntity entity;
    @SuppressWarnings("unused")
    private Component1 componentAdd1;
    @SuppressWarnings("unused")
    private Component2 componentAdd2;
    private Component3 firstAddedComponent3;
    private Component4 firstAddedComponent4;
    private long addComponentTime;
    private EcsAssertions assertions;
    private boolean inited = false;

    @Test
    void addMultipleComponentsSimultaneously() {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E12.getId());
        componentAdd1 = entity.getComponent(Component1.class);
        componentAdd2 = entity.getComponent(Component2.class);
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在同一帧内连续添加多个组件
        if (currentTime == addComponentTime) {
            // 第一个组件
            firstAddedComponent3 = new Component3();
            boolean addResult1 = entity.addComponent(firstAddedComponent3);
            assert addResult1 : "First component add should succeed";
            
            // 验证获取的ComponentAdd3是同一个对象实例
            Component3 retrieved3 = entity.getComponent(Component3.class);
            assert retrieved3 == firstAddedComponent3 : "Retrieved ComponentAdd3 should be the same instance as the one added";
            
            // 第二个组件
            firstAddedComponent4 = new Component4();
            boolean addResult2 = entity.addComponent(firstAddedComponent4);
            assert addResult2 : "Second component add should succeed";
            
            // 验证获取的ComponentAdd4是同一个对象实例
            Component4 retrieved4 = entity.getComponent(Component4.class);
            assert retrieved4 == firstAddedComponent4 : "Retrieved ComponentAdd4 should be the same instance as the one added";
            
            // 再次验证ComponentAdd3仍然是同一个实例
            Component3 retrieved3Again = entity.getComponent(Component3.class);
            assert retrieved3Again == firstAddedComponent3 : "ComponentAdd3 should still be the same instance after adding ComponentAdd4";
            
            log.info("Added ComponentAdd3 and ComponentAdd4 at time: {}", currentTime);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证多个组件同时添加
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
            // 在添加时刻，Component3和Component4都已添加
            // Spawn阶段：SystemSpawnDefaultComponent3添加"o3"，SystemSpawnDefaultComponent4添加"o4"
            // Logic阶段：SystemDefaultComponent1添加"1"，SystemDefaultComponent2添加"2"
            //           SystemDefaultComponent3添加"3"，SystemDefaultComponent4添加"4"
            // SystemDefaultComponentLexicographic将cache复制到data
            Component3 comp3 = entity.getComponent(Component3.class);
            assert comp3 != null : "Component3 should be added at time " + currentTime;
            // 验证是同一个对象实例
            assert comp3 == firstAddedComponent3 : "Component3 should be the same instance as the one added";
            
            Component4 comp4 = entity.getComponent(Component4.class);
            assert comp4 != null : "Component4 should be added at time " + currentTime;
            // 验证是同一个对象实例
            assert comp4 == firstAddedComponent4 : "Component4 should be the same instance as the one added";
            
            // 验证ComponentLexicographic.data包含所有组件的数据（包含spawn阶段的"o3"和"o4"）
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o3o41234");
        } else if (currentTime >= addComponentTime + interval) {
            // 验证两个组件都被添加
            Component3 comp3 = entity.getComponent(Component3.class);
            Component4 comp4 = entity.getComponent(Component4.class);
            assert comp3 != null : "Component3 should be added at time " + currentTime;
            assert comp4 != null : "Component4 should be added at time " + currentTime;
            
            // 验证仍然是同一个对象实例
            assert comp3 == firstAddedComponent3 : "Component3 should still be the same instance at time " + currentTime;
            assert comp4 == firstAddedComponent4 : "Component4 should still be the same instance at time " + currentTime;
            
            // 验证ComponentLexicographic.data包含所有组件的数据（spawn阶段只在初始化时执行一次）
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1234");
        }
    }
}

