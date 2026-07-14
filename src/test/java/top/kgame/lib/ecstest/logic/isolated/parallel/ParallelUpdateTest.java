package top.kgame.lib.ecstest.logic.isolated.parallel;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.logic.util.EcsTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParallelUpdateTest extends EcsTestBase {
    private static final int ENTITY_COUNT = 64;
    private static final int UPDATE_FRAMES = 3;

    @Test
    void parallelUpdateProcessesAllEntities() {
        EcsEntity[] entities = new EcsEntity[ENTITY_COUNT];
        for (int i = 0; i < ENTITY_COUNT; i++) {
            entities[i] = ecsWorld.createEntity(ParallelEntityFactory.class);
        }

        updateWorld(0, DEFAULT_INTERVAL * UPDATE_FRAMES, DEFAULT_INTERVAL);

        for (EcsEntity entity : entities) {
            ComponentParallelCounter counter = entity.getComponent(ComponentParallelCounter.class);
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
