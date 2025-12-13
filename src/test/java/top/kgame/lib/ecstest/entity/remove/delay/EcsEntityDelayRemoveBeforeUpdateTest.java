package top.kgame.lib.ecstest.entity.remove.delay;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandDestroyEntity;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

/**
 * Entity延迟移除测试用例 - 在beforeUpdate中延迟移除实体
 * 注意：在beforeUpdate中只能使用WORLD级别的延迟命令（通过ecsWorld.addDelayCommand）
 */
public class EcsEntityDelayRemoveBeforeUpdateTest extends EcsTestBase {
    private EcsEntity entity1;
    private EcsEntity entity12;
    private EcsEntity entity123;
    private EcsAssertions assertions;
    private long removeEntityTime1;
    private long removeEntityTime12;
    private long removeEntityTime123;

    @Test
    void removeEntityInBeforeUpdate() {
        assertions = new EcsAssertions(ecsWorld);
        
        // 先创建实体
        entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());
        entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());
        entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        
        final int interval = DEFAULT_INTERVAL;
        removeEntityTime1 = interval * 30;
        removeEntityTime12 = interval * 40;
        removeEntityTime123 = interval * 50;
        
        // 执行更新循环
        updateWorld(0, interval * 100, interval);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        // 在beforeUpdate中延迟移除实体（使用WORLD级别）
        if (currentTime == removeEntityTime1) {
            ecsWorld.addDelayCommand(new EcsCommandDestroyEntity(ecsWorld, entity1));
        }
        if (currentTime == removeEntityTime12) {
            ecsWorld.addDelayCommand(new EcsCommandDestroyEntity(ecsWorld, entity12));
        }
        if (currentTime == removeEntityTime123) {
            ecsWorld.addDelayCommand(new EcsCommandDestroyEntity(ecsWorld, entity123));
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        // 验证实体在移除前存在，在移除后不存在
        if (currentTime < removeEntityTime1) {
            assertions.assertEntityExists(entity1, currentTime);
        }
        if (currentTime >= removeEntityTime1) {
            assertions.assertEntityNotExists(entity1, currentTime);
        }

        if (currentTime < removeEntityTime12) {
            assertions.assertEntityExists(entity12, currentTime);
        }
        if (currentTime >= removeEntityTime12) {
            assertions.assertEntityNotExists(entity12, currentTime);
        }

        if (currentTime < removeEntityTime123) {
            assertions.assertEntityExists(entity123, currentTime);
        }
        if (currentTime >= removeEntityTime123) {
            assertions.assertEntityNotExists(entity123, currentTime);
        }
    }
}
