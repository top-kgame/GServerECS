package top.kgame.lib.ecstest.component.add.delay;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandAddComponent;
import top.kgame.lib.ecs.command.EcsCommandRemoveComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟添加测试用例 - 添加后立即移除
 */
class EcsComponentDelayAddThenRemoveTest extends EcsTestBase {
    private EcsEntity entity;
    private ComponentCommandHolder componentCommand;
    private long addComponentTime;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;
    private boolean inited = false;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void addThenRemoveComponent(EcsCommandScope scope) {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        componentCommand = entity.getComponent(ComponentCommandHolder.class);
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
        // 先添加组件，然后移除
        if (currentTime == addComponentTime) {
            componentCommand.update(new EcsCommandAddComponent(entity, new Component2()), commandScope);
        }
        // 在下一个interval移除组件
        if (currentTime == addComponentTime + interval) {
            componentCommand.update(new EcsCommandRemoveComponent(entity, Component2.class), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证添加后立即移除
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o11");
        } else if (currentTime < addComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1");
        } else if (currentTime == addComponentTime) {
            Component2 comp2 = entity.getComponent(Component2.class);
            switch (commandScope) {
                case SYSTEM -> {
                    assert comp2 != null : "Component2 should be added";
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
                }
                case SYSTEM_GROUP -> {
                    assert comp2 != null : "Component2 should be added";
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1");
                }
                case WORLD -> {
                    assert comp2 != null : "Component2 should be added";
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1");
                }
            }
            // 组件应该被添加

        } else if (currentTime == addComponentTime + interval ) {
            Component2 comp2 = entity.getComponent(Component2.class);
            switch (commandScope) {
                case SYSTEM -> {
                    assert comp2 == null : "Component2 should be added";
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o21");
                }
                case SYSTEM_GROUP -> {
                    assert comp2 == null : "Component2 should be added";
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o212");
                }
                case WORLD -> {
                    assert comp2 == null : "Component2 should be added";
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o212");
                }
            }
        } else if (currentTime > addComponentTime + interval ) {
            Component2 comp2 = entity.getComponent(Component2.class);
            assert comp2 == null : "Component2 should be added";
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1");
        }
    }
}

