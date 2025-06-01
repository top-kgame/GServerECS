package top.kgame.lib.ecstest.component.remove.delay;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandRemoveComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component4;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟移除测试用例 - 移除不存在的组件
 */
class EcsComponentDelayRemoveNonExistentTest extends EcsTestBase {
    private EcsEntity entity;
    private ComponentCommandHolder componentCommandHolder;
    private long removeComponentTime;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;
    private boolean inited = false;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void removeNonExistentComponent(EcsCommandScope scope) {
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
        if (currentTime == removeComponentTime) {
            // 尝试移除一个不存在的组件类型（Component4不存在于E123实体上）
            componentCommandHolder.update(new EcsCommandRemoveComponent(entity, Component4.class), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证移除不存在的组件应该失败，组件状态不变
        // 无论什么scope，移除不存在的组件都应该失败，状态保持不变
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o2o3123");
        } else if (currentTime < removeComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        } else if (currentTime == removeComponentTime) {
            // 在移除时刻，根据不同的scope判断状态
            // 由于Component4不存在，无论什么scope，状态都应该保持不变
            String expectedData = switch (commandScope) {
                case SYSTEM -> "123";  // SYSTEM scope: 移除不存在的组件失败，状态不变
                case SYSTEM_GROUP -> "123";  // SYSTEM_GROUP scope: 移除不存在的组件失败，状态不变
                case WORLD -> "123";  // WORLD scope: 移除不存在的组件失败，状态不变
            };
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", expectedData);
            // 验证Component4确实不存在
            Component4 comp4 = entity.getComponent(Component4.class);
            assert comp4 == null : "Component4 should not exist at time " + currentTime + " with scope " + commandScope;
        } else {
            // 移除命令执行后，由于组件不存在，移除应该失败，状态不变
            ComponentCommandHolder holder = entity.getComponent(ComponentCommandHolder.class);
            assert holder != null : "ComponentCommandHolder should still exist at time " + currentTime + " with scope " + commandScope;
            // 验证Component4仍然不存在
            Component4 comp4 = entity.getComponent(Component4.class);
            assert comp4 == null : "Component4 should still not exist at time " + currentTime + " with scope " + commandScope;
            // 验证组件状态不变（通过检查data字段，应该仍然是123）
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        }
    }
}

