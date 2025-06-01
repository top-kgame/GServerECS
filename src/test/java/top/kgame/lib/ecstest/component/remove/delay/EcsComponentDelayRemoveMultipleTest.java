package top.kgame.lib.ecstest.component.remove.delay;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandRemoveComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟移除测试用例 - 多次移除同一个组件
 */
class EcsComponentDelayRemoveMultipleTest extends EcsTestBase {
    private EcsEntity entity;
    private ComponentCommandHolder componentCommandHolder;
    private long removeComponentTime1;
    private long removeComponentTime2;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;
    private boolean inited = false;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void removeComponentMultipleTimes(EcsCommandScope scope) {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E123.getId());
        componentCommandHolder = entity.getComponent(ComponentCommandHolder.class);
        commandScope = scope;
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        removeComponentTime1 = interval * 30;
        // 第二次移除时间需要根据scope调整，确保在第一次移除完成后才执行第二次移除
        // 对于SYSTEM和SYSTEM_GROUP，组件会在系统/组执行完成后移除（需要1个interval）
        // 对于WORLD，组件会在世界更新完成后移除（需要1个interval）
        removeComponentTime2 = interval * 35; // 第二次移除，确保在第一次移除完成后
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 第一次移除组件
        if (currentTime == removeComponentTime1) {
            componentCommandHolder.update(new EcsCommandRemoveComponent(entity, Component3.class), commandScope);
        }
        // 第二次移除同一个组件（此时组件应该已经被移除）
        if (currentTime == removeComponentTime2) {
            componentCommandHolder.update(new EcsCommandRemoveComponent(entity, Component3.class), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o2o3123");
        } else if (currentTime < removeComponentTime1) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        } else if (currentTime == removeComponentTime1) {
            // 在移除时刻，根据不同的scope判断组件是否已被移除
            String expectedData = switch (commandScope) {
                case SYSTEM -> "12";  // SYSTEM scope: Component3在SystemDefaultComponent3之后被移除，所以此时只有12
                case SYSTEM_GROUP -> "12";  // SYSTEM_GROUP scope: 在组结束后移除，此时只有12
                case WORLD -> "123";  // WORLD scope: 在世界更新结束后移除，此时还有3
            };
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", expectedData);
        } else if (currentTime >= removeComponentTime1 + interval && currentTime < removeComponentTime2) {
            // 第一次移除后，组件应该已被移除，SystemDefaultComponent3不再运行
            Component3 comp3 = entity.getComponent(Component3.class);
            Assertions.assertNull(
                    comp3,
                    "Component3 should be removed after first remove at time " + currentTime + " with scope " + commandScope
            );
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        } else if (currentTime == removeComponentTime2) {
            // 第二次移除时刻，组件已经不存在，但命令会被设置
            // 此时状态应该和之前一样（组件已被移除）
            Component3 comp3 = entity.getComponent(Component3.class);
            Assertions.assertNull(
                    comp3,
                    "Component3 should still be null at second remove time " + currentTime + " with scope " + commandScope
            );
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        } else if (currentTime >= removeComponentTime2 + interval) {
            // 第二次移除不存在的组件应该失败，状态不变
            Component3 comp3 = entity.getComponent(Component3.class);
            Assertions.assertNull(
                    comp3,
                    "Component3 should still be null after second remove at time " + currentTime + " with scope " + commandScope
            );
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        }
    }
}

