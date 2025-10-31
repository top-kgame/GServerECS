package top.kgame.lib.ecstest.component.remove.immediately;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Component立即移除测试用例 - 移除多个组件
 */
class EcsComponentRemoveMultipleTest extends EcsTestBase {
    private static final Logger log = LogManager.getLogger(EcsComponentRemoveMultipleTest.class);
    private EcsEntity entity;
    @SuppressWarnings("unused")
    private Component2 componentRemove2; // 保留用于向后兼容，实际使用 entity.getComponent() 获取
    private long endTime;
    private long removeComponentTime;
    private EcsAssertions assertions;
    private boolean inited = false;
    private boolean destroy = false;
    private boolean removeFirstComponent = false;
    private boolean removeSecondComponent = false;

    @Test
    void removeMultipleComponents() {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E123.getId());
        componentRemove2 = entity.getComponent(Component2.class);
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        destroy = false;
        removeFirstComponent = false;
        removeSecondComponent = false;
        
        final int interval = DEFAULT_INTERVAL;
        endTime = interval * 100;
        removeComponentTime = interval * 30;
        
        // 执行更新循环
        updateWorld(0, endTime, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间移除第一个组件
        if (!removeFirstComponent && currentTime == removeComponentTime) {
            entity.removeComponent(Component3.class);
            log.info("remove Component3");
            removeFirstComponent = true;
        }
        
        // 在下一个interval移除第二个组件
        if (!removeSecondComponent && currentTime == removeComponentTime + interval) {
            entity.removeComponent(Component2.class);
            log.info("remove Component2");
            removeSecondComponent = true;
        }
        
        // 在指定时间请求销毁实体
        if (currentTime >= endTime - interval * 10 && !destroy) {
            destroy = true;
            ecsWorld.requestDestroyEntity(entity);
            log.info("request destroy entity");
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (!destroy) {
            Component2 currentComp2 = entity.getComponent(Component2.class);
            if (currentComp2 != null) {
                log.info("update result: {}", currentComp2.data);
            }
            
            if (!inited) {
                inited = true;
                assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o2o3123");
            } else if (currentTime < removeComponentTime) {
                if (currentComp2 != null) {
                    log.info("update result: {}", currentComp2.data);
                }
                assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
            } else if (currentTime == removeComponentTime) {
                // 移除第一个组件
                assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
                Component3 comp3 = entity.getComponent(Component3.class);
                assert comp3 == null : "Component3 should be removed";
            } else if (currentTime == removeComponentTime + interval) {
                // 移除第二个组件
                Component2 comp2 = entity.getComponent(Component2.class);
                assert comp2 == null : "Component2 should be removed";
                Component3 comp3 = entity.getComponent(Component3.class);
                assert comp3 == null : "Component3 should be removed";
            } else if (currentTime > removeComponentTime + interval) {
                // 两个组件都已移除
                Component2 comp2 = entity.getComponent(Component2.class);
                assert comp2 == null : "Component2 should be removed";
                Component3 comp3 = entity.getComponent(Component3.class);
                assert comp3 == null : "Component3 should be removed";
            }
        } else {
            Component2 found = entity.getComponent(Component2.class);
            assert found == null : "Component should be null after entity destroy";
        }
    }
}

