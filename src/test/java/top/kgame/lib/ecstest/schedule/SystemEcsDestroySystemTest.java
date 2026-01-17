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
    private long destroyTime;

    @Test
    void testEcsDestroySystem() {
        // 用world context承接销毁系统的结果，避免afterUpdate访问已销毁entity
        ecsWorld.setContext(new AtomicBoolean(false));
        
        // 创建实体
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        assertNotNull(entity, "Entity should be created");
        
        destroyTime = DEFAULT_INTERVAL * 5;
        
        updateWorld(0, DEFAULT_INTERVAL * 10, DEFAULT_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间请求销毁实体
        if (currentTime == destroyTime && entity != null) {
            ecsWorld.requestDestroyEntity(entity);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        AtomicBoolean destroyedFlag = ecsWorld.getContext();
        if (currentTime < destroyTime) {
            // 销毁前：实体应存在
            assertFalse(destroyedFlag.get(), "Context should not be marked before destruction");
        } else {
            // 销毁时刻及之后：在afterUpdate时entity已经被销毁；通过context校验销毁系统已执行
            assertTrue(destroyedFlag.get(), "Context should be marked by destroy system after destruction");
        }
    }
}
