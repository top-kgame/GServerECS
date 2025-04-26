package top.kgame.lib.ecstest.component.add.delay;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandAddComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟添加测试用例 - 基本场景（不同作用域）
 */
class EcsComponentDelayAddTest extends EcsTestBase {
    private EcsEntity entity;
    private ComponentCommandHolder componentCommand;
    private Component2 component2;
    private long addComponentTime;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;
    private boolean inited = false;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void addComponentWithDifferentScopes(EcsCommandScope scope) {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E12.getId());
        componentCommand = entity.getComponent(ComponentCommandHolder.class);
        component2 = entity.getComponent(Component2.class);
        commandScope = scope;
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间设置延迟添加组件的命令
        if (currentTime == addComponentTime) {
            componentCommand.update(new EcsCommandAddComponent(entity, new Component3()), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 根据不同的 scope 和当前时间进行断言
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o212");
        } else if (currentTime < addComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        } else if (currentTime == addComponentTime) {
            // 不同 scope 在添加时刻的期望值不同
            String expectedData = switch (commandScope) {
                case SYSTEM -> "123";
                case SYSTEM_GROUP -> "123";
                case WORLD -> "12";
            };
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", expectedData);
        } else if (currentTime == addComponentTime + interval) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o3123");
        } else {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        }
    }
}