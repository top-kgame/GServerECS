package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 实体立即添加后验证系统执行测试
 */
public class EcsEntityAddWithSystemExecutionTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private long addEntityTime;

    @Test
    void addEntityAndVerifySystemExecution() {
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 立即添加实体（包含Component1，会被SystemDefaultComponent1处理）
        if (currentTime == addEntityTime) {
            entity = ecsWorld.createEntity(EntityIndex.E1.getId());
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体存在
        if (currentTime >= addEntityTime) {
            assertions.assertEntityExists(entity, currentTime);
            
            // 验证系统已执行（ComponentLexicographic的cache应该被更新）
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            assertNotNull(lex, "Entity should contain ComponentLexicographic");
            // 系统会在更新时修改cache，所以应该与初始值不同
            if (currentTime > addEntityTime) {
                assertNotNull(lex.cache, "System should have updated ComponentLexicographic");
            }
        }
    }
}
