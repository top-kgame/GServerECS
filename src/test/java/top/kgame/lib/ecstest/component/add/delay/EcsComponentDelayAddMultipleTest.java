package top.kgame.lib.ecstest.component.add.delay;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandAddComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.*;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟添加测试用例 - 多个组件同时添加
 */
class EcsComponentDelayAddMultipleTest extends EcsTestBase {
    private EcsEntity entity;
    private Component1 component1;
    private long addComponentTime;
    private EcsAssertions assertions;
    private boolean inited = false;
    private EcsCommandScope scope;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void addMultipleComponentsSimultaneously(EcsCommandScope scope) {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        component1 = entity.getComponent(Component1.class);
        this.scope = scope;
        // 添加命令持有者组件
        entity.addComponent(new ComponentCommandHolder());
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 同时添加多个组件
        if (currentTime == addComponentTime) {
            // 第一个组件直接添加
            entity.addComponent(new Component2());
            // 第二个组件通过ComponentDelayAddCommandHolder添加
            ComponentCommandHolder holder = entity.getComponent(ComponentCommandHolder.class);
            if (holder != null) {
                holder.update(new EcsCommandAddComponent(entity, new Component3()), scope);
            }
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证多个组件同时添加
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o11");
        } else if (currentTime < addComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1");
        } else if (currentTime == addComponentTime) {
            // 验证两个组件都被添加
            Component2 comp2 = entity.getComponent(Component2.class);
            Component3 comp3 = entity.getComponent(Component3.class);
            switch (scope) {
                case SYSTEM -> {
                    assert comp2 != null : "ComponentDelayAdd3 should be added at time " + currentTime;
                    assert comp3 != null : "ComponentDelayAdd4 should be added at time " + currentTime;
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o2123");
                }
                case WORLD -> {
                    assert comp2 != null : "ComponentDelayAdd3 should be added at time " + currentTime;
                    assert comp3 != null : "ComponentDelayAdd4 should be added at time " + currentTime;
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o212");
                }
                case SYSTEM_GROUP -> {
                    assert comp2 != null : "ComponentDelayAdd3 should be added at time " + currentTime;
                    assert comp3 != null : "ComponentDelayAdd4 should be added at time " + currentTime;
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o2123");
                }
            }

        } else if (currentTime == addComponentTime + interval) {
            // 验证两个组件都被添加
            Component2 comp2 = entity.getComponent(Component2.class);
            Component3 comp3 = entity.getComponent(Component3.class);
            switch (scope) {
                case SYSTEM -> {
                    assert comp2 != null : "ComponentDelayAdd3 should be added at time " + currentTime;
                    assert comp3 != null : "ComponentDelayAdd4 should be added at time " + currentTime;
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o3123");
                }
                case WORLD -> {
                    assert comp2 != null : "ComponentDelayAdd3 should be added at time " + currentTime;
                    assert comp3 != null : "ComponentDelayAdd4 should be null at time " + currentTime;
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o3123");
                }
                case SYSTEM_GROUP -> {
                    assert comp2 != null : "ComponentDelayAdd3 should be added at time " + currentTime;
                    assert comp3 != null : "ComponentDelayAdd4 should be null at time " + currentTime;
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o3123");
                }
            }
        } else if (currentTime > addComponentTime + interval) {
            // 验证两个组件都被添加
            Component2 comp2 = entity.getComponent(Component2.class);
            Component3 comp3 = entity.getComponent(Component3.class);
            assert comp2 != null : "ComponentDelayAdd3 should be added at time " + currentTime;
            assert comp3 != null : "ComponentDelayAdd4 should be null at time " + currentTime;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        }
    }
}

