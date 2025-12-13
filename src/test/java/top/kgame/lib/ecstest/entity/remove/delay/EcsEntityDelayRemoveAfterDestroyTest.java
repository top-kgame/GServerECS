package top.kgame.lib.ecstest.entity.remove.delay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * Entity延迟移除测试用例 - 实体销毁后延迟移除
 * 测试所有EcsCommandScope情况
 * 注意：此测试验证在实体已被销毁后，延迟移除命令的行为
 */
public class EcsEntityDelayRemoveAfterDestroyTest extends EcsTestBase {
    private static final Logger log = LogManager.getLogger(EcsEntityDelayRemoveAfterDestroyTest.class);
    
    public static class LogicContext {
        public EcsEntity entity1;
        public EcsEntity entity123; // 触发实体
        public long createTime = 0;
        public long destroyTime = 0;
        public EcsCommandScope level = null;
    }

    private LogicContext context;
    private EcsAssertions assertions;
    private long destroyEntityTime;
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
    void removeEntityAfterDestroyed(EcsCommandScope scope) {
        // 初始化测试数据
        context.level = scope;
        
        // 先创建实体
        context.entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        context.entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        
        final int interval = DEFAULT_INTERVAL;
        destroyEntityTime = interval * 30;
        removeEntityTime = destroyEntityTime + interval * 5; // 在销毁后尝试延迟移除实体
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 先销毁实体
        if (currentTime == destroyEntityTime) {
            context.destroyTime = currentTime;
            ecsWorld.requestDestroyEntity(context.entity1);
        }
        // 在实体销毁后尝试延迟移除实体
        if (currentTime == removeEntityTime) {
            context.createTime = currentTime;
            // 尝试在已销毁的实体上执行延迟移除命令（这应该被忽略或无效）
            commandHolder.update(new EcsCommandDestroyEntity(ecsWorld, context.entity1), context.level);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体销毁后移除实体的行为
        if (currentTime < destroyEntityTime) {
            assertions.assertEntityExists(context.entity1, currentTime);
        } else if (currentTime >= destroyEntityTime + interval) {
            // 实体应该已被销毁
            EcsEntity found = ecsWorld.getEntity(context.entity1.getIndex());
            assert found == null : "Entity1 should be destroyed after destroyTime " + destroyEntityTime + " at time " + currentTime;
            
            // 在销毁后尝试移除的实体应该已经被销毁（再次销毁应该被忽略）
            if (currentTime >= removeEntityTime) {
                log.info("EcsEntity destroyed as expected, delay remove command should be ignored at time {}", currentTime);
            }
        }
    }
}
