package top.kgame.lib.ecstest.component.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component立即移除测试用例 - 移除不存在的组件
 */
class EcsComponentRemoveNonExistentTest extends EcsTestBase {
    private EcsEntity entity;
    private Component2 componentRemove2;
    private long endTime;
    private long removeComponentTime;
    private EcsAssertions assertions;
    private boolean inited = false;
    private boolean destroy = false;
    private boolean removeComponent = false;

    @Test
    void removeNonExistentComponent() {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E123.getId());
        componentRemove2 = entity.getComponent(Component2.class);
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        destroy = false;
        removeComponent = false;
        
        final int interval = DEFAULT_INTERVAL;
        endTime = interval * 100;
        removeComponentTime = interval * 30;
        
        // 执行更新循环
        updateWorld(0, endTime, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间移除组件，然后尝试再次移除（应该返回null）
        if (!removeComponent && currentTime == removeComponentTime) {
            Component3 removed = (Component3) entity.removeComponent(Component3.class);
            assert removed != null : "Component should exist before first removal";
            // 再次尝试移除同一个组件（应该返回null）
            Component3 removedAgain = (Component3) entity.removeComponent(Component3.class);
            assert removedAgain == null : "Component should be null after removal";
            System.out.println("remove non-existent Component3 (already removed)");
            removeComponent = true;
        }
        
        // 在指定时间请求销毁实体
        if (currentTime >= endTime - interval * 10 && !destroy) {
            destroy = true;
            ecsWorld.requestDestroyEntity(entity);
            System.out.println("request destroy entity");
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (!destroy) {
            System.out.println("update result: " + componentRemove2.data);
            
            if (!inited) {
                inited = true;
                assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o2o3123");
            } else if (currentTime < removeComponentTime) {
                System.out.println("update result: " + componentRemove2.data);
                assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
            } else if (currentTime == removeComponentTime) {
                // 在beforeUpdate中移除，移除后立即生效
                assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
                Component3 comp3 = entity.getComponent(Component3.class);
                assert comp3 == null : "Component3 should be removed";
            } else if (currentTime > removeComponentTime) {
                if (!removeComponent) {
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
                } else {
                    assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
                }
            }
        } else {
            Component2 found = entity.getComponent(Component2.class);
            assert found == null : "Component should be null after entity destroy";
        }
    }
}

