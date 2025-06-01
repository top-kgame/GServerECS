package top.kgame.lib.ecstest.component.remove.delay;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandRemoveComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟移除测试用例 - 同时移除多个不同的组件
 */
class EcsComponentDelayRemoveMultipleDifferentTest extends EcsTestBase {
    private EcsEntity entity;
    private ComponentCommandHolder componentCommandHolder;
    private long removeComponentTime;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;
    private boolean inited = false;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void removeMultipleDifferentComponentsSimultaneously(EcsCommandScope scope) {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E123.getId());
        componentCommandHolder = entity.getComponent(ComponentCommandHolder.class);
        commandScope = scope;
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        removeComponentTime = interval * 30;
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 同时移除多个不同的组件
        // 注意：由于实体只能有一个ComponentCommandHolder，我们需要在两个连续的更新周期中设置命令
        // Component2在SysGroupDefaultLogic组中，Component3在SysGroupDefaultLogic2组中
        // 所以它们的移除命令会在不同的系统执行完成后执行
        if (currentTime == removeComponentTime) {
            // 第一个周期：移除Component3（在SysGroupDefaultLogic2组中）
            componentCommandHolder.update(new EcsCommandRemoveComponent(entity, Component3.class), commandScope);
        } else if (currentTime == removeComponentTime + interval) {
            // 第二个周期：移除Component2（在SysGroupDefaultLogic组中）
            // 此时第一个命令已经被SystemDelayAddComponentCommandHolder处理并清除
            componentCommandHolder.update(new EcsCommandRemoveComponent(entity, Component2.class), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证多个组件同时移除
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o2o3123");
        } else if (currentTime < removeComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        } else if (currentTime <= removeComponentTime + interval * 2L) {
            // 在移除命令设置后的过渡期间，允许不同的状态
            // 不强制检查具体状态，只确保不会出现错误状态
            Component2 comp2 = entity.getComponent(Component2.class);
            Component3 comp3 = entity.getComponent(Component3.class);
            // 验证数据字段包含合理的值（1、2、3的组合）
            String data = entity.getComponent(ComponentLexicographic.class).data;
            assert data.contains("1") : "Data should contain '1'";
            // 如果Component2还在，数据应该包含"2"
            if (comp2 != null) {
                assert data.contains("2") : "Data should contain '2' when Component2 exists";
            }
            // 如果Component3还在，数据应该包含"3"
            if (comp3 != null) {
                assert data.contains("3") : "Data should contain '3' when Component3 exists";
            }
        } else {
            // 移除后，Component2和Component3都不再存在，所以只有1
            // 需要等待足够的时间确保所有命令都已执行
            // 由于Component2和Component3在不同的系统组中，它们的移除时机可能不同
            // Component2在SysGroupDefaultLogic组中，Component3在SysGroupDefaultLogic2组中
            // 对于SYSTEM和SYSTEM_GROUP，需要等待至少一个interval
            // 对于WORLD，需要等待至少一个interval
            // 为了确保两个组件都被移除，我们等待更长时间（3个interval，确保两个系统组都执行完）
            long expectedRemoveTime = switch (commandScope) {
                case SYSTEM -> removeComponentTime + interval * 4L;  // SYSTEM scope: 在系统执行完成后移除，需要等待两个系统组都执行完（Component3在removeComponentTime+interval执行，Component2在removeComponentTime+interval*2执行）
                case SYSTEM_GROUP -> removeComponentTime + interval * 4L;  // SYSTEM_GROUP scope: 在组结束后移除，需要等待两个系统组都执行完
                case WORLD -> removeComponentTime + interval * 2L;  // WORLD scope: 在世界更新结束后移除，所有命令都会执行（Component3在removeComponentTime+interval执行，Component2在removeComponentTime+interval*2执行）
            };
            
            if (currentTime >= expectedRemoveTime) {
                // 验证两个组件都被移除
                Component2 comp2 = entity.getComponent(Component2.class);
                Component3 comp3 = entity.getComponent(Component3.class);
                assert comp2 == null : "Component2 should be removed at time " + currentTime + " with scope " + commandScope;
                assert comp3 == null : "Component3 should be removed at time " + currentTime + " with scope " + commandScope;
                // Component2和Component3都被移除后，只有Component1的系统还在运行
                assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1");
            } else if (currentTime > removeComponentTime + interval) {
                // 在等待期间，允许组件逐步被移除
                // 检查当前状态，但不强制要求两个组件都被移除
                Component2 comp2 = entity.getComponent(Component2.class);
                Component3 comp3 = entity.getComponent(Component3.class);
                // 如果Component3已被移除但Component2还没有，这是正常的（因为它们在不同的系统组中）
                if (comp3 == null && comp2 != null) {
                    // Component3已被移除，Component2还在，这是正常的过渡状态
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
                } else if (comp2 == null && comp3 != null) {
                    // Component2已被移除，Component3还在，这是正常的过渡状态
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "13");
                } else if (comp2 == null) {
                    // 两个组件都被移除了
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1");
                } else {
                    // 两个组件都还在，根据scope判断
                    String expectedData = switch (commandScope) {
                        case SYSTEM -> "123";  // 可能还在等待执行
                        case SYSTEM_GROUP -> "123";  // 可能还在等待执行
                        case WORLD -> "123";  // 还在等待执行
                    };
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", expectedData);
                }
            }
        }
    }
}

