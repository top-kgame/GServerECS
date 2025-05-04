package top.kgame.lib.ecstest.component.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component立即添加测试用例 - 添加已存在的组件
 */
class EcsComponentAddExistingTest extends EcsTestBase {
    private EcsEntity entity;
    private Component2 firstAddedComponent;
    private long addComponentTime;
    private EcsAssertions assertions;
    private boolean inited = false;

    @Test
    void addExistingComponent() {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        addComponentTime = interval * 30;

        firstAddedComponent = new Component2();
        boolean addResult = entity.addComponent(firstAddedComponent);
        assert addResult : "First add should succeed";
        
        // 验证获取的组件是同一个对象实例
        Component2 retrieved = entity.getComponent(Component2.class);
        assert retrieved == firstAddedComponent : "Retrieved component should be the same instance as the one added";
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 尝试添加已存在的组件
        if (currentTime == addComponentTime) {
            Component2 existing = entity.getComponent(Component2.class);
            assert existing != null : "ComponentAdd3 should exist before attempting to add again";
            assert existing == firstAddedComponent : "Existing component should be the same instance as the first added";
            
            // 尝试再次添加已存在的组件，应该返回false
            Component2 newComponent = new Component2();
            boolean addResult = entity.addComponent(newComponent);
            assert !addResult : "Adding existing component should return false";
            
            // 验证尝试添加后，获取的组件仍然是原来的实例，而不是新创建的实例
            Component2 stillExisting = entity.getComponent(Component2.class);
            assert stillExisting == firstAddedComponent : "Component should still be the original instance after failed add attempt";
            assert stillExisting != newComponent : "Component should not be replaced by the new instance";
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证添加已存在的组件应该失败，组件状态不变
        if (!inited) {
            inited = true;
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o212");
        } else if (currentTime < addComponentTime) {
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        } else if (currentTime >= addComponentTime) {
            // 添加已存在的组件应该失败，ComponentAdd3应该已经存在且只有一个实例
            Component2 existing = entity.getComponent(Component2.class);
            assert existing != null : "ComponentAdd3 should exist";
            // 验证组件没有被重复添加（通过检查data字段，应该只有初始的a1，没有因为重复添加而触发额外的系统更新）
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        }
    }
}

