package top.kgame.lib.ecstest.dispose;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ECS销毁测试用例 - 多个实体在关闭时的清理
 * 测试场景：验证多个实体在关闭时都被正确清理
 */
class EcsDisposeMultipleEntitiesTest extends EcsTestBase {
    private List<EcsEntity> entities = new ArrayList<>();
    private List<Component1> components1 = new ArrayList<>();
    private List<Component2> components2 = new ArrayList<>();
    private List<Component3> components3 = new ArrayList<>();
    private static final int ENTITY_COUNT = 10;
    private static long beforeCloseTime = 0;
    private static long afterCloseTime = 0;

    @Test
    void multipleEntitiesDisposeBeforeUpdate() {
        // 测试场景：在 update 前关闭，多个实体应该被清理
        createMultipleEntities();
        
        final int tickInterval = DEFAULT_INTERVAL;
        long endTime = 60000;
        beforeCloseTime = 300;
        afterCloseTime = endTime;
        
        updateWorld(0, endTime, tickInterval);
        
        // 断言：所有实体应该被清理
        verifyAllEntitiesCleaned();
    }

    @Test
    void multipleEntitiesDisposeAfterUpdate() {
        // 测试场景：在 update 后关闭，多个实体应该被清理
        createMultipleEntities();
        
        final int tickInterval = DEFAULT_INTERVAL;
        long endTime = 60000;
        beforeCloseTime = endTime;
        afterCloseTime = 300;

        updateWorld(0, endTime, tickInterval);
        
        // 断言：所有实体应该被清理
        verifyAllEntitiesCleaned();
    }

    @Test
    void multipleEntitiesDisposeByTopSystem() {
        // 测试场景：TopSystem内部触发关闭，多个实体应该被清理
        createMultipleEntities();
        final int tickInterval = DEFAULT_INTERVAL;
        long endTime = 60000;
        ecsWorld.setContext(new DisposeContext(300, endTime));
        
        updateWorld(0, endTime, tickInterval);
        
        // 断言：所有实体应该被清理
        verifyAllEntitiesCleaned();
    }

    @Test
    void multipleEntitiesDisposeByGroupSystem() {
        // 测试场景：GroupSystem内部触发关闭，多个实体应该被清理
        createMultipleEntities();
        final int tickInterval = DEFAULT_INTERVAL;
        long endTime = 60000;
        ecsWorld.setContext(new DisposeContext(endTime, 300));

        updateWorld(0, endTime, tickInterval);

        // 断言：所有实体应该被清理
        verifyAllEntitiesCleaned();
    }

    private void createMultipleEntities() {
        entities.clear();
        components1.clear();
        components2.clear();
        components3.clear();
        
        for (int i = 0; i < ENTITY_COUNT; i++) {
            EcsEntity entity = ecsWorld.createEntity(EntityIndex.E123.getId());
            entities.add(entity);
            components1.add(entity.getComponent(Component1.class));
            components2.add(entity.getComponent(Component2.class));
            components3.add(entity.getComponent(Component3.class));
        }
        
        // 验证所有实体都已创建
        assert entities.size() == ENTITY_COUNT : "Should create " + ENTITY_COUNT + " entities";
        Collection<EcsEntity> allEntities = ecsWorld.getAllEntity();
        assert allEntities.size() >= ENTITY_COUNT : "World should have at least " + ENTITY_COUNT + " entities";
    }

    private void verifyAllEntitiesCleaned() {
        // 验证所有实体都已被清理
        assert ecsWorld.isClosed() : "World should be closed";
        
        Collection<EcsEntity> allEntities = ecsWorld.getAllEntity();
        assert allEntities.isEmpty() : "All entities should be cleaned after close, but found " + allEntities.size() + " entities";
        
        // 验证通过索引获取实体也返回 null
        for (EcsEntity entity : entities) {
            EcsEntity found = ecsWorld.getEntity(entity.getIndex());
            assert found == null : "Entity " + entity.getIndex() + " should have been cleaned";
        }
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 场景：close 在 update 前
        if (currentTime > beforeCloseTime && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 场景：close 在 update 后
        if (currentTime > afterCloseTime && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }
}

