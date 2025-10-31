package top.kgame.lib.ecstest.component.remove.delay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandRemoveComponent;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * 延迟移除Component测试用例
 */
class EcsComponentDelayRemoveTest extends EcsTestBase {
    private static final Logger log = LogManager.getLogger(EcsComponentDelayRemoveTest.class);
    private EcsEntity entity;
    private ComponentCommandHolder componentCommandHolder;
    private Component2 componentDelayRemove2;
    private long removeComponentTime;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;
    private boolean inited = false;

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void removeComponentWithDifferentScopes(EcsCommandScope scope) {
        // 初始化测试数据
        entity = ecsWorld.createEntity(EntityIndex.E123.getId());
        componentCommandHolder = entity.getComponent(ComponentCommandHolder.class);
        componentDelayRemove2 = entity.getComponent(Component2.class);
        commandScope = scope;
        assertions = new EcsAssertions(ecsWorld);
        inited = false;
        
        final int interval = DEFAULT_INTERVAL;
        removeComponentTime = interval * 30;
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间设置延迟移除组件的命令
        if (currentTime == removeComponentTime) {
            componentCommandHolder.update(new EcsCommandRemoveComponent(entity, Component3.class), commandScope);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        ComponentLexicographic lexicographic = entity.getComponent(ComponentLexicographic.class);
        if (lexicographic != null) {
            log.info("update result: {}", lexicographic.data);
        }
        
        // 根据不同的 scope 和当前时间进行断言
        if (!inited) {
            inited = true;
            // 初始化时：spawn阶段添加o1o2o3，logic阶段添加123
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "o1o2o3123");
        } else if (currentTime < removeComponentTime) {
            if (lexicographic != null) {
                log.info("update result: {}", lexicographic.data);
            }
            // 正常更新时：只有logic阶段的123（spawn只在初始化时执行一次）
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "123");
        } else if (currentTime == removeComponentTime) {
            // 不同 scope 在移除时刻的期望值不同
            // 在移除时刻，Component3可能还未被移除（取决于scope）
            String expectedData = switch (commandScope) {
                case SYSTEM -> "12";  // SYSTEM scope: Component3在SystemDefaultComponent3之后被移除，所以此时只有12
                case SYSTEM_GROUP -> "12";  // SYSTEM_GROUP scope: 在组结束后移除，此时只有12
                case WORLD -> "123";  // WORLD scope: 在世界更新结束后移除，此时还有3
            };
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", expectedData);
        } else {
            // 移除后，Component3不再存在，SystemDefaultComponent3不再运行，所以只有12
            assertions.assertComponentField(entity, ComponentLexicographic.class, "data", "12");
        }
    }
} 