package top.kgame.lib.ecstest.entity.add.immediately;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Entity延迟添加测试用例 - 多个实体同时延迟添加
 */
public class EcsEcsEntityAddMultipleBeforeUpdateTest extends EcsTestBase {
    public static class LogicContext {
        public EcsEntity entity1;
        public EcsEntity entity12;
        public EcsEntity entity123;
        public long createTime = 0;
        public EcsCommandScope level = null;
    }

    private LogicContext context;
    private EcsCommandScope commandScope;
    private EcsAssertions assertions;
    private long addEntityTime;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        context = new LogicContext();
        ecsWorld.setContext(context);
    }

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void addMultipleEntitiesSimultaneously(EcsCommandScope scope) {
        // 初始化测试数据
        context.level = scope;
        commandScope = scope;
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime = interval * 30;
        context.createTime = addEntityTime;
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在指定时间同时添加多个实体
        if (currentTime == addEntityTime) {
            context.entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
            context.entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
            context.entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证多个实体同时被添加
        if (currentTime >= addEntityTime) {
            assertions.assertEntityExists(context.entity1, currentTime);
            assertions.assertEntityExists(context.entity12, currentTime);
            assertions.assertEntityExists(context.entity123, currentTime);
        }
    }
}

