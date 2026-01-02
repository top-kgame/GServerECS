package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.extensions.component.DestroyingComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EcsDestroySystem测试用例
 * 测试实体销毁系统的功能
 */
class SystemEcsDestroySystemTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long destroyTime;

    @BeforeEach
    @Override
    protected void setUp() {
        // 只扫描本用例需要的System，避免影响其他system包下用例的执行顺序
        ecsWorld = EcsWorld.generateInstance("top.kgame.lib.ecstest.destroy", "top.kgame.lib.ecstest.util");
    }

    @Test
    void testEcsDestroySystem() {
        assertions = new EcsAssertions(ecsWorld);

        // 用world context承接销毁系统的结果，避免afterUpdate访问已销毁entity
        ecsWorld.setContext(new AtomicBoolean(false));
        
        // 创建实体
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        assertNotNull(entity, "实体应被创建");
        
        destroyTime = DEFAULT_INTERVAL * 5;
        
        updateWorld(0, DEFAULT_INTERVAL * 10, DEFAULT_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间请求销毁实体
        if (currentTime == destroyTime && entity != null) {
            ecsWorld.requestDestroyEntity(entity);
            // 验证DestroyingComponent已被添加
            assertNotNull(entity.getComponent(DestroyingComponent.class), 
                "请求销毁后，实体应包含DestroyingComponent");
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        AtomicBoolean destroyedFlag = ecsWorld.getContext();
        if (currentTime < destroyTime) {
            // 销毁前：实体应存在
            assertions.assertEntityExists(entity, currentTime);
            assertFalse(destroyedFlag.get(), "销毁前context不应被标记");
        } else {
            // 销毁时刻及之后：在afterUpdate时entity已经被销毁；通过context校验销毁系统已执行
            assertions.assertEntityNotExists(entity, currentTime);
            assertTrue(destroyedFlag.get(), "销毁后context应被销毁系统标记");
        }
    }
}
