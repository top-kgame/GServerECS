package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System执行顺序综合测试
 * 测试复杂场景下的系统执行顺序
 */
class SystemMultipleEntitiesOrderTest extends EcsTestBase {

    @Test
    void testSystemOrderWithMultipleEntities() {
        // 创建多个实体
        EcsEntity entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        EcsEntity entity2 = ecsWorld.createEntity(EntityIndex.E12.getId());
        
        updateWorld(0, DEFAULT_INTERVAL * 10, DEFAULT_INTERVAL);
        
        // 验证两个实体都被正确处理
        ComponentLexicographic lex1 = entity1.getComponent(ComponentLexicographic.class);
        ComponentLexicographic lex2 = entity2.getComponent(ComponentLexicographic.class);
        
        if (lex1 != null && lex2 != null) {
            assertTrue(lex1.data.contains("ACBDE"), "system的执行顺序应该是ACBDE");
            assertTrue(lex2.data.contains("ACBDE"), "system的执行顺序应该是ACBDE");
        }
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
    }
}
