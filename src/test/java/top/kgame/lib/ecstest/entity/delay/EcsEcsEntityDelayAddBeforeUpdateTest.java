package top.kgame.lib.ecstest.entity.delay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandCreateEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Entity延迟添加测试用例 - 在beforeUpdate中延迟添加实体
 * 注意：在beforeUpdate中只能使用WORLD级别的延迟命令（通过ecsWorld.addDelayCommand）
 */
public class EcsEcsEntityDelayAddBeforeUpdateTest extends EcsTestBase {
    public static class LogicContext {
        public EcsEntity entity1;
        public EcsEntity entity12;
        public EcsEntity entity123;
    }

    private LogicContext context;
    private EcsAssertions assertions;
    private long addEntityTime1;
    private long addEntityTime12;
    private long addEntityTime123;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        context = new LogicContext();
        ecsWorld.setContext(context);
    }

    @Test
    void addEntityInBeforeUpdate() {
        // 初始化测试数据
        assertions = new EcsAssertions(ecsWorld);
        
        final int interval = DEFAULT_INTERVAL;
        addEntityTime1 = interval * 30;
        addEntityTime12 = interval * 40;
        addEntityTime123 = interval * 50;
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在beforeUpdate中延迟添加实体（使用WORLD级别）
        if (currentTime == addEntityTime1) {
            ecsWorld.addDelayCommand(new EcsCommandCreateEntity(ecsWorld, EntityIndex.E1.getId(), it -> {context.entity1 = it;}));
        }
        if (currentTime == addEntityTime12) {
            ecsWorld.addDelayCommand(new EcsCommandCreateEntity(ecsWorld, EntityIndex.E12.getId(), it -> {context.entity12 = it;}));
        }
        if (currentTime == addEntityTime123) {
            ecsWorld.addDelayCommand(new EcsCommandCreateEntity(ecsWorld, EntityIndex.E123.getId(), it -> {context.entity123 = it;}));
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (currentTime >= addEntityTime1) {
            assertions.assertEntityExists(context.entity1, currentTime);
        }

        if (currentTime >= addEntityTime12) {
            assertions.assertEntityExists(context.entity12, currentTime);
        }
        // 对 entity123 的断言
        if (currentTime >= addEntityTime123) {
            assertions.assertEntityExists(context.entity123, currentTime);
        }
    }
}

