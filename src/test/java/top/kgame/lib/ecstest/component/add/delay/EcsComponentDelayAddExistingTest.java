package top.kgame.lib.ecstest.component.add.delay;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandAddComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟添加测试用例 - 添加已存在的组件
 */
class EcsComponentDelayAddExistingTest extends EcsTestBase {
    private EcsEntity entity;
    private ComponentCommandHolder commandHolder;
    private EcsCommandScope commandScope;
    private long addComponentTime;
    private EcsAssertions assertions;
    private boolean inited = false;
    private Component2 component2;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void addExistingComponent(EcsCommandScope scope) {
        // 初始化测试数据 包含ComponentDelayAdd1和ComponentDelayAdd2
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        commandHolder = entity.getComponent(ComponentCommandHolder.class);
        commandScope = scope;
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;
        
        // 先立即添加ComponentDelayAdd3
        component2 = new Component2();
        entity.addComponent(component2);
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 尝试添加已存在的组件
        if (currentTime == addComponentTime) {
            commandHolder.update(new EcsCommandAddComponent(entity, new Component2()), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证添加已存在的组件应该失败，组件状态不变
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o212");
        } else if (currentTime < addComponentTime) {
            // 在addComponentTime之前，data应该保持为"a1"（SystemDelayAddComponent4每次更新都会复制并清空）
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        } else if (currentTime >= addComponentTime) {
            // 添加已存在的组件应该失败，ComponentDelayAdd3应该已经存在
            Component2 existing = entity.getComponent(Component2.class);
            assert existing != null : "ComponentDelayAdd3 should exist";
            assert component2 == existing : "ComponentDelayAdd3 should be the same";
            // 验证组件没有被重复添加（通过检查data字段，应该保持为"a1"）
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        }
    }
}

