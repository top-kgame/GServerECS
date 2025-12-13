package top.kgame.lib.ecstest.entity.remove.delay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandDestroyEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Entity延迟移除测试用例 - 在系统中延迟移除多个实体
 * 测试所有EcsCommandScope情况
 */
public class EcsEntityDelayRemoveMultipleInSystemTest extends EcsTestBase {
    public static class LogicContext {
        public EcsEntity entity1;
        public EcsEntity entity12;
        public EcsEntity entity123;
        public EcsCommandScope level = null;
    }

    private LogicContext context;
    private EcsAssertions assertions;
    private long removeEntityTime;
    private ComponentCommandHolder commandHolder;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        context = new LogicContext();
        ecsWorld.setContext(context);
        assertions = new EcsAssertions(ecsWorld);
        // 创建一个用于持有命令的实体
        EcsEntity entity = ecsWorld.createEntity(EntityIndex.E0.getId());
        commandHolder = entity.getComponent(ComponentCommandHolder.class);
    }

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void removeMultipleEntitiesInSystem(EcsCommandScope scope) {
        // 初始化测试数据
        context.level = scope;
        
        // 先创建实体
        context.entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        context.entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
        context.entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime = interval * 30;
        
        // 执行更新循环
        updateWorld(0, interval * 60, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间同时设置延迟移除多个实体的命令
        if (currentTime == removeEntityTime) {
            commandHolder.update(new EcsCommandDestroyEntity(ecsWorld, context.entity1), context.level);
            commandHolder.update(new EcsCommandDestroyEntity(ecsWorld, context.entity12), context.level);
            commandHolder.update(new EcsCommandDestroyEntity(ecsWorld, context.entity123), context.level);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 根据不同的 scope 和当前时间进行断言
        switch (context.level) {
            case SYSTEM -> {
                // SYSTEM scope: 在当前System执行完成后移除
                if (currentTime == removeEntityTime) {
                    // 在当前update中，实体应该已被移除
                    assertions.assertEntityNotExists(context.entity1, currentTime);
                    assertions.assertEntityNotExists(context.entity12, currentTime);
                    assertions.assertEntityNotExists(context.entity123, currentTime);
                }
                if (currentTime > removeEntityTime) {
                    assertions.assertEntityNotExists(context.entity1, currentTime);
                    assertions.assertEntityNotExists(context.entity12, currentTime);
                    assertions.assertEntityNotExists(context.entity123, currentTime);
                }
            }
            case SYSTEM_GROUP -> {
                // SYSTEM_GROUP scope: 在当前系统组执行完成后移除
                if (currentTime == removeEntityTime) {
                    // 在当前update中，实体应该已被移除
                    assertions.assertEntityNotExists(context.entity1, currentTime);
                    assertions.assertEntityNotExists(context.entity12, currentTime);
                    assertions.assertEntityNotExists(context.entity123, currentTime);
                }
                if (currentTime > removeEntityTime) {
                    assertions.assertEntityNotExists(context.entity1, currentTime);
                    assertions.assertEntityNotExists(context.entity12, currentTime);
                    assertions.assertEntityNotExists(context.entity123, currentTime);
                }
            }
            case WORLD -> {
                // WORLD scope: 在本次世界update完成后移除
                if (currentTime == removeEntityTime) {
                    // 在当前update中，实体可能还存在（因为是在WORLD update完成后移除）
                    // 但根据实现，WORLD级别的命令在当前update结束后执行，所以下一个update时实体应该不存在
                }
                if (currentTime > removeEntityTime) {
                    assertions.assertEntityNotExists(context.entity1, currentTime);
                    assertions.assertEntityNotExists(context.entity12, currentTime);
                    assertions.assertEntityNotExists(context.entity123, currentTime);
                }
            }
        }
        
        // 验证在移除时间之前的实体存在
        if (currentTime < removeEntityTime) {
            assertions.assertEntityExists(context.entity1, currentTime);
            assertions.assertEntityExists(context.entity12, currentTime);
            assertions.assertEntityExists(context.entity123, currentTime);
        }
    }
}
