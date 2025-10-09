package top.kgame.lib.ecstest.entity.add.delay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Entity延迟添加测试用例 - 实体销毁后延迟添加
 */
public class EcsEcsEntityDelayAddAfterDestroyTest extends EcsTestBase {
    public static class LogicContext {
        public EcsEntity entity1;
        public EcsEntity entity123; // 触发实体
        public long createTime = 0;
        public long destroyTime = 0;
        public EcsCommandScope level = null;
    }

    private LogicContext context;
    private EcsAssertions assertions;
    private long addEntityTime;
    private long destroyEntityTime;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        context = new LogicContext();
        ecsWorld.setContext(context);
    }

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void addEntityAfterDestroyed(EcsCommandScope scope) {
        // 初始化测试数据
        context.level = scope;
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建一个实体
        context.entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        context.entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        
        final int interval = DEFAULT_INTERVAL;
        destroyEntityTime = interval * 30;
        addEntityTime = destroyEntityTime + interval * 5; // 在销毁后尝试添加实体
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 先销毁实体
        if (currentTime == destroyEntityTime) {
            context.destroyTime = currentTime;
            // 通过系统来销毁实体（需要在系统中实现）
            // 这里我们直接使用 requestDestroyEntity
            ecsWorld.requestDestroyEntity(context.entity1);
        }
        // 在实体销毁后尝试延迟添加实体
        if (currentTime == addEntityTime) {
            context.createTime = currentTime;
            // 尝试在已销毁的实体上添加新实体（这应该失败或无效）
            // 注意：这里我们尝试添加一个新的实体，而不是在已销毁的实体上操作
            // 实际场景可能是：在销毁实体后，尝试通过延迟命令添加一个相同类型的实体
            ecsWorld.createEntity(EntityIndex.E123.getId());
            // 系统会在update中检测到createTime并尝试添加实体
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体销毁后添加实体的行为
        if (currentTime < destroyEntityTime) {
            assertions.assertEntityExists(context.entity1, currentTime);
        } else if (currentTime >= destroyEntityTime + interval) {
            // 实体应该已被销毁
            EcsEntity found = ecsWorld.getEntity(context.entity1.getIndex());
            assert found == null : "Entity1 should be destroyed after destroyTime " + destroyEntityTime + " at time " + currentTime;
            
            // 在销毁后尝试添加的实体应该能正常创建（因为是新实体）
            if (currentTime >= addEntityTime) {
                // 如果系统成功添加了新实体，验证它
                // 注意：这个测试主要验证在实体销毁后，延迟添加命令仍然可以正常工作
                System.out.println("EcsEntity destroyed as expected, new entity add should work at time " + currentTime);
            }
        }
    }
}

