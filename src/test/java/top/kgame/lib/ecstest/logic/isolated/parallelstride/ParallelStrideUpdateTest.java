package top.kgame.lib.ecstest.logic.isolated.parallelstride;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecstest.logic.util.EcsTestBase;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParallelStrideUpdateTest extends EcsTestBase {
    /** 需超过并行执行器最小批次粒度，确保走真实并行路径。 */
    private static final int ENTITY_COUNT = 512;
    private static final int UPDATE_FRAMES = 3;

    @Test
    void strideParallelUpdateProcessesAllEntities() {
        EcsEntity[] entities = new EcsEntity[ENTITY_COUNT];
        for (int i = 0; i < ENTITY_COUNT; i++) {
            entities[i] = ecsWorld.createEntity(StrideEntityFactory.class);
        }

        updateWorld(0, DEFAULT_INTERVAL * UPDATE_FRAMES, DEFAULT_INTERVAL);

        for (EcsEntity entity : entities) {
            ComponentStrideCounter counter = entity.getComponent(ComponentStrideCounter.class);
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
