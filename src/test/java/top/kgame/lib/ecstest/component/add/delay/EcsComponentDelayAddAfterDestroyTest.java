package top.kgame.lib.ecstest.component.add.delay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandAddComponent;
import top.kgame.lib.ecs.command.EcsCommandDestroyEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component延迟添加测试用例 - 实体销毁后添加组件
 */
class EcsComponentDelayAddAfterDestroyTest extends EcsTestBase {
    private static final Logger log = LogManager.getLogger(EcsComponentDelayAddAfterDestroyTest.class);
    private EcsEntity entity;
    private ComponentCommandHolder commandHolder;
    private long addComponentTime;
    private long destroyTime;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void addComponentAfterEntityDestroyed(EcsCommandScope scope) {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        commandHolder = entity.getComponent(ComponentCommandHolder.class);
        commandScope = scope;
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;
        destroyTime = addComponentTime - DEFAULT_INTERVAL * 5; // 在添加组件之前销毁实体
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在实体销毁后尝试添加组件
        if (currentTime == destroyTime) {
            // 先销毁实体
            commandHolder.update(new EcsCommandDestroyEntity(ecsWorld, entity), commandScope);
        } else if (currentTime == addComponentTime) {
            // 尝试在实体销毁后添加组件
            commandHolder.update(new EcsCommandAddComponent(entity, new Component3()), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体销毁后添加组件的行为
        if (currentTime < destroyTime) {
            assertions.assertEntityExists(entity, currentTime);
        } else if (currentTime >= destroyTime + DEFAULT_INTERVAL) {
            // 实体应该已被销毁
            EcsEntity found = ecsWorld.getEntity(entity.getIndex());
            assert found == null : "EcsEntity should be destroyed after destroyTime " + destroyTime + " at time " + currentTime;
            // 尝试在已销毁的实体上添加组件应该失败或无效
            log.info("EcsEntity destroyed as expected, component add should fail at time {}", currentTime);
        }
    }
}

