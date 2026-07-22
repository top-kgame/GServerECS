package top.kgame.lib.ecstest.logic.isolated.parallelcustom;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.logic.util.EcsTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParallelCustomStrategyUpdateTest extends EcsTestBase {
    private static final int ENTITY_COUNT = 512;
    private static final int UPDATE_FRAMES = 3;

    @Test
    void customStrategyParallelUpdateProcessesAllEntities() {
        EcsEntity[] entities = new EcsEntity[ENTITY_COUNT];
        for (int i = 0; i < ENTITY_COUNT; i++) {
            entities[i] = ecsWorld.createEntity(CustomParallelEntityFactory.class);
        }

        updateWorld(0, DEFAULT_INTERVAL * UPDATE_FRAMES, DEFAULT_INTERVAL);

        for (EcsEntity entity : entities) {
            ComponentCustomParallelCounter counter =
                    entity.getComponent(ComponentCustomParallelCounter.class);
            assertEquals(UPDATE_FRAMES, counter.count);
        }
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
    }
}
