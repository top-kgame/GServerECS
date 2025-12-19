package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 实体立即移除时验证组件测试
 */
public class EcsEntityRemoveWithComponentVerificationTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long removeEntityTime;

    @Test
    void removeEntityWithComponentVerification() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 创建实体
        entity = ecsWorld.createEntity(EntityIndex.E12.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即移除实体
        if (currentTime == removeEntityTime) {
            // 在移除前验证组件存在
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            assert lex != null : "ComponentLexicographic should exist before remove";
            
            ecsWorld.requestDestroyEntity(entity);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体在移除前存在，在移除后不存在
        if (currentTime < removeEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
        }
        if (currentTime >= removeEntityTime) {
            assertions.assertEntityNotExists(entity, currentTime);
        }
    }
}
