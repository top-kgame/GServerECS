package top.kgame.lib.ecstest.logic.isolated.invalidparallel;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.exception.InvalidParallelUpdateAnnotationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ParallelUpdateInvalidAnnotationTest {
    @Test
    void parallelUpdateCannotBeUsedOnSystemGroup() {
        assertThrows(InvalidParallelUpdateAnnotationException.class, () ->
                EcsWorld.generateInstance("top.kgame.lib.ecstest.logic.isolated.invalidparallel"));
    }
}
