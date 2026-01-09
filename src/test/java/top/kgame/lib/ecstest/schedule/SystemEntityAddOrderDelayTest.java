package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandCreateEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Entity添加对System执行顺序影响的测试 - 延迟模式
 */
class SystemEntityAddOrderDelayTest extends EcsTestBase {
    private ComponentCommandHolder componentCommandHolder;
    private ComponentLexicographic componentLexicographic;
    private long modifyTime;
    private EcsCommandScope commandScope;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void testDelayAddEntity(EcsCommandScope scope) {
        EcsEntity commandEntity = ecsWorld.createEntity(EntityIndex.E1.getId());
        componentCommandHolder = commandEntity.getComponent(ComponentCommandHolder.class);
        componentLexicographic = commandEntity.getComponent(ComponentLexicographic.class);
        commandScope = scope;
        modifyTime = DEFAULT_INTERVAL * 10;

        updateWorld(0, DEFAULT_INTERVAL * 20, DEFAULT_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        if (componentCommandHolder != null && currentTime == modifyTime) {
            componentCommandHolder.update(
                new EcsCommandCreateEntity(ecsWorld, EntityIndex.E1.getId(),
                    it -> {}),
                commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        assertTrue(componentLexicographic.data.contains("ACBDE"),
                "system的执行顺序不应该受EcsCommandCreateEntity影响");
    }
}

