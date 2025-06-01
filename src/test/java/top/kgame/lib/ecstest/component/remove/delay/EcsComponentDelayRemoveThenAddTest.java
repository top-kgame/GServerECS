package top.kgame.lib.ecstest.component.remove.delay;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandAddComponent;
import top.kgame.lib.ecs.command.EcsCommandRemoveComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟移除测试用例 - 移除后再次添加该组件
 */
class EcsComponentDelayRemoveThenAddTest extends EcsTestBase {
    private EcsEntity entity;
    private ComponentCommandHolder componentCommandHolder;
    private long removeComponentTime;
    private long addComponentTime;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;
    private boolean inited = false;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void removeThenAddComponent(EcsCommandScope scope) {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E123.getId());
        componentCommandHolder = entity.getComponent(ComponentCommandHolder.class);
        commandScope = scope;
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        removeComponentTime = interval * 30;
        addComponentTime = removeComponentTime + interval * 5; // 在移除后添加组件
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 先移除组件
        if (currentTime == removeComponentTime) {
            componentCommandHolder.update(new EcsCommandRemoveComponent(entity, Component3.class), commandScope);
        }
        // 在移除后再次添加该组件
        if (currentTime == addComponentTime) {
            componentCommandHolder.update(new EcsCommandAddComponent(entity, new Component3()), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证移除后再次添加
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o2o3123");
        } else if (currentTime < removeComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        } else if (currentTime >= removeComponentTime + interval && currentTime < addComponentTime) {
            // 移除后，组件应该已被移除，SystemDefaultComponent3不再运行
            Component3 comp3 = entity.getComponent(Component3.class);
            assert comp3 == null : "Component3 should be removed";
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        } else if (currentTime >= addComponentTime + interval) {
            // 再次添加后，组件应该存在，SystemDefaultComponent3重新运行
            Component3 comp3 = entity.getComponent(Component3.class);
            assert comp3 != null : "Component3 should be added again";
            // 添加后，所有系统都运行，数据恢复为123
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        }
    }
}

