package top.kgame.lib.ecstest.entity.delay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.command.EcsCommandCreateEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Entity延迟添加测试用例 - 在beforeUpdate中延迟添加实体
 * 注意：在beforeUpdate中只能使用WORLD级别的延迟命令（通过ecsWorld.addDelayCommand）
 */
public class EcsEcsEntityDelayAddInSystemTest extends EcsTestBase {
    public static class LogicContext {
        public EcsEntity entity1;
        public EcsEntity entity12;
        public EcsEntity entity123;
        public EcsCommandScope level = null;
    }

    private LogicContext context;
    private EcsAssertions assertions;
    private long addEntityTime1;
    private long addEntityTime12;
    private long addEntityTime123;
    private ComponentCommandHolder commandHolder;

    @BeforeEach
    @Override
    protected void setUp() {
        super.setUp();
        context = new LogicContext();
        ecsWorld.setContext(context);
        assertions = new EcsAssertions(ecsWorld);
        EcsEntity entity = ecsWorld.createEntity(EntityIndex.E0.getId());
        commandHolder = entity.getComponent(ComponentCommandHolder.class);
    }

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void addEntityInBeforeUpdate(EcsCommandScope scope) {
        // 初始化测试数据
        context.level = scope;
        
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
            commandHolder.update(new EcsCommandCreateEntity(ecsWorld, EntityIndex.E1.getId(), it -> {context.entity1 = it;}), context.level);
        }
        if (currentTime == addEntityTime12) {
            commandHolder.update(new EcsCommandCreateEntity(ecsWorld, EntityIndex.E12.getId(), it -> {context.entity12 = it;}), context.level);
        }
        if (currentTime == addEntityTime123) {
            commandHolder.update(new EcsCommandCreateEntity(ecsWorld, EntityIndex.E123.getId(), it -> {context.entity123 = it;}), context.level);
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        switch (context.level) {
            case SYSTEM -> {
                if (currentTime == addEntityTime1) {
                    assertions.assertEntityExists(context.entity1, currentTime);
                    assertions.assertComponentField(context.entity1, ComponentLexicographic.class, "data", "1");
                }
                if (currentTime == addEntityTime1 + interval) {
                    assertions.assertEntityExists(context.entity1, currentTime);
                    assertions.assertComponentField(context.entity1, ComponentLexicographic.class, "data", "o11");
                }
                if (currentTime > addEntityTime1 + interval) {
                    assertions.assertEntityExists(context.entity1, currentTime);
                    assertions.assertComponentField(context.entity1, ComponentLexicographic.class, "data", "1");
                }

                if (currentTime == addEntityTime12) {
                    assertions.assertEntityExists(context.entity12, currentTime);
                    assertions.assertComponentField(context.entity12, ComponentLexicographic.class, "data", "12");
                }
                if (currentTime == addEntityTime12 + interval) {
                    assertions.assertEntityExists(context.entity12, currentTime);
                    assertions.assertComponentField(context.entity12, ComponentLexicographic.class, "data", "o1o212");
                }
                if (currentTime > addEntityTime12 + interval) {
                    assertions.assertEntityExists(context.entity12, currentTime);
                    assertions.assertComponentField(context.entity12, ComponentLexicographic.class, "data", "12");
                }

                if (currentTime == addEntityTime123) {
                    assertions.assertEntityExists(context.entity123, currentTime);
                    assertions.assertComponentField(context.entity123, ComponentLexicographic.class, "data", "123");
                }
                if (currentTime == addEntityTime123 + interval) {
                    assertions.assertEntityExists(context.entity123, currentTime);
                    assertions.assertComponentField(context.entity123, ComponentLexicographic.class, "data", "o1o2o3123");
                }
                if (currentTime > addEntityTime123 + interval) {
                    assertions.assertEntityExists(context.entity123, currentTime);
                    assertions.assertComponentField(context.entity123, ComponentLexicographic.class, "data", "123");
                }
            }
            case SYSTEM_GROUP -> {
                if (currentTime == addEntityTime1) {
                    assertions.assertEntityExists(context.entity1, currentTime);
                    assertions.assertComponentField(context.entity1, ComponentLexicographic.class, "data", "");
                }
                if (currentTime == addEntityTime1 + interval) {
                    assertions.assertEntityExists(context.entity1, currentTime);
                    assertions.assertComponentField(context.entity1, ComponentLexicographic.class, "data", "o11");
                }
                if (currentTime > addEntityTime1 + interval) {
                    assertions.assertEntityExists(context.entity1, currentTime);
                    assertions.assertComponentField(context.entity1, ComponentLexicographic.class, "data", "1");
                }

                if (currentTime == addEntityTime12) {
                    assertions.assertEntityExists(context.entity12, currentTime);
                    assertions.assertComponentField(context.entity12, ComponentLexicographic.class, "data", "");
                }
                if (currentTime == addEntityTime12 + interval) {
                    assertions.assertEntityExists(context.entity12, currentTime);
                    assertions.assertComponentField(context.entity12, ComponentLexicographic.class, "data", "o1o212");
                }
                if (currentTime > addEntityTime12 + interval) {
                    assertions.assertEntityExists(context.entity12, currentTime);
                    assertions.assertComponentField(context.entity12, ComponentLexicographic.class, "data", "12");
                }

                // 对 entity123 的断言
                if (currentTime == addEntityTime123) {
                    assertions.assertEntityExists(context.entity123, currentTime);
                    assertions.assertComponentField(context.entity123, ComponentLexicographic.class, "data", "3");
                }
                if (currentTime == addEntityTime123 + interval) {
                    assertions.assertEntityExists(context.entity123, currentTime);
                    assertions.assertComponentField(context.entity123, ComponentLexicographic.class, "data", "o1o2o3123");
                }
                if (currentTime > addEntityTime123 + interval) {
                    assertions.assertEntityExists(context.entity123, currentTime);
                    assertions.assertComponentField(context.entity123, ComponentLexicographic.class, "data", "123");
                }
            }
            case WORLD -> {
                if (currentTime == addEntityTime1) {
                    assertions.assertEntityExists(context.entity1, currentTime);
                    assertions.assertComponentField(context.entity1, ComponentLexicographic.class, "data", "");
                }
                if (currentTime == addEntityTime1 + interval) {
                    assertions.assertEntityExists(context.entity1, currentTime);
                    assertions.assertComponentField(context.entity1, ComponentLexicographic.class, "data", "o11");
                }
                if (currentTime > addEntityTime1 + interval) {
                    assertions.assertEntityExists(context.entity1, currentTime);
                    assertions.assertComponentField(context.entity1, ComponentLexicographic.class, "data", "1");
                }

                if (currentTime == addEntityTime12) {
                    assertions.assertEntityExists(context.entity12, currentTime);
                    assertions.assertComponentField(context.entity12, ComponentLexicographic.class, "data", "");
                }
                if (currentTime == addEntityTime12 + interval) {
                    assertions.assertEntityExists(context.entity12, currentTime);
                    assertions.assertComponentField(context.entity12, ComponentLexicographic.class, "data", "o1o212");
                }
                if (currentTime > addEntityTime12 + interval) {
                    assertions.assertEntityExists(context.entity12, currentTime);
                    assertions.assertComponentField(context.entity12, ComponentLexicographic.class, "data", "12");
                }

                // 对 entity123 的断言
                if (currentTime == addEntityTime123) {
                    assertions.assertEntityExists(context.entity123, currentTime);
                    assertions.assertComponentField(context.entity123, ComponentLexicographic.class, "data", "");
                }
                if (currentTime == addEntityTime123 + interval) {
                    assertions.assertEntityExists(context.entity123, currentTime);
                    assertions.assertComponentField(context.entity123, ComponentLexicographic.class, "data", "o1o2o3123");
                }
                if (currentTime > addEntityTime123 + interval) {
                    assertions.assertEntityExists(context.entity123, currentTime);
                    assertions.assertComponentField(context.entity123, ComponentLexicographic.class, "data", "123");
                }
            }
        }
    }
}

