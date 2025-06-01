package top.kgame.lib.ecstest.component.remove.delay;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandDestroyEntity;
import top.kgame.lib.ecs.command.EcsCommandRemoveComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟移除测试用例 - 实体销毁后移除组件
 */
class EcsComponentDelayRemoveAfterDestroyTest extends EcsTestBase {
    private EcsEntity entity;
    private ComponentCommandHolder componentCommandHolder;
    private long removeComponentTime;
    private long destroyTime;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void removeComponentAfterEntityDestroyed(EcsCommandScope scope) {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E123.getId());
        componentCommandHolder = entity.getComponent(ComponentCommandHolder.class);
        commandScope = scope;
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        removeComponentTime = interval * 30;
        destroyTime = removeComponentTime - DEFAULT_INTERVAL * 5; // 在移除组件之前销毁实体
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在实体销毁后尝试移除组件
        if (currentTime == destroyTime) {
            // 先销毁实体
            componentCommandHolder.update(new EcsCommandDestroyEntity(ecsWorld, entity), EcsCommandScope.SYSTEM);
        } else if (currentTime == removeComponentTime) {
            // 尝试在实体销毁后移除组件
            componentCommandHolder.update(new EcsCommandRemoveComponent(entity, Component3.class), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体销毁后移除组件的行为
        if (currentTime < destroyTime) {
            assertions.assertEntityExists(entity, currentTime);
        } else if (currentTime >= destroyTime + DEFAULT_INTERVAL) {
            // 实体应该已被销毁
            EcsEntity found = ecsWorld.getEntity(entity.getIndex());
            assert found == null : "EcsEntity should be destroyed after destroyTime " + destroyTime + " at time " + currentTime;
            // 尝试在已销毁的实体上移除组件应该失败或无效
            System.out.println("EcsEntity destroyed as expected, component remove should fail at time " + currentTime);
        }
    }
}

