package top.kgame.lib.ecstest.entity.remove.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 实体立即移除后验证系统执行测试
 */
public class EcsEntityRemoveWithSystemExecutionTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long removeEntityTime;

    @Test
    void removeEntityAndVerifySystemExecution() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 创建实体（包含Component1，会被SystemDefaultComponent1处理）
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即移除实体
        if (currentTime == removeEntityTime) {
            // 在移除前验证系统已执行（ComponentLexicographic应该被更新）
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            assertNotNull(lex, "Entity should contain ComponentLexicographic");
            
            ecsWorld.requestDestroyEntity(entity);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体在移除前存在，在移除后不存在
        if (currentTime < removeEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            assertNotNull(lex, "Entity should contain ComponentLexicographic");
        }
        if (currentTime >= removeEntityTime) {
            assertions.assertEntityNotExists(entity, currentTime);
        }
    }
}
