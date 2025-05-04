package top.kgame.lib.ecstest.component.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component立即添加测试用例 - 实体销毁后添加组件
 */
class EcsComponentAddAfterDestroyTest extends EcsTestBase {
    private EcsEntity entity;
    private long addComponentTime;
    private long destroyTime;
    private EcsAssertions assertions;
    private boolean inited = false;

    @Test
    void addComponentAfterEntityDestroyed() {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;
        destroyTime = addComponentTime - DEFAULT_INTERVAL * 5; // 在添加组件之前销毁实体
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 先销毁实体
        if (currentTime == destroyTime) {
            ecsWorld.requestDestroyEntity(entity);
            System.out.println("EcsEntity destroyed at time: " + currentTime);
        }
        
        // 尝试在实体销毁后添加组件
        if (currentTime == addComponentTime) {
            // 尝试在实体销毁后添加组件 注意：实体可能已经被销毁，但引用可能还存在
            EcsEntity found = ecsWorld.getEntity(entity.getIndex());
            if (found != null) {
                // 如果实体还存在，尝试添加组件
                boolean addResult = found.addComponent(new Component2());
                System.out.println("Attempted to add component to destroyed entity, result: " + addResult);
            } else {
                System.out.println("EcsEntity not found, cannot add component");
            }
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体销毁后添加组件的行为
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o11");
        } else if (currentTime < destroyTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "1");
            assertions.assertEntityExists(entity, currentTime);
        } else if (currentTime >= destroyTime + interval) {
            // 实体应该已被销毁
            EcsEntity found = ecsWorld.getEntity(entity.getIndex());
            assert found == null : "EcsEntity should be destroyed after destroyTime " + destroyTime + " at time " + currentTime;
            // 尝试在已销毁的实体上添加组件应该失败或无效
            System.out.println("EcsEntity destroyed as expected, component add should fail at time " + currentTime);
        }
    }
}

