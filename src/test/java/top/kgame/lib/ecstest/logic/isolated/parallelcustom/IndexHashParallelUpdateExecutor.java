package top.kgame.lib.ecstest.logic.isolated.parallelcustom;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.ParallelUpdateExecutor;
import top.kgame.lib.ecs.core.ParallelUpdateExecutorManager;

import java.util.List;
import java.util.function.Consumer;

/** 按 entity index 取模分片的自定义执行器。 */
public final class IndexHashParallelUpdateExecutor extends ParallelUpdateExecutor {
    public IndexHashParallelUpdateExecutor(ParallelUpdateExecutorManager manager) {
        super(manager);
    }

    @Override
    public void forEach(List<EcsEntity> entities,
                        Consumer<EcsEntity> action,
                        int minEntitiesPerBatch) {
        final int size = entities.size();
        if (size == 0) {
            return;
        }
        final int taskCount = resolveTaskCount(size, minEntitiesPerBatch);
        if (taskCount == 1) {
            entities.forEach(action);
            return;
        }
        runParallel(taskCount, taskIndex -> {
            for (int i = 0; i < size; i++) {
                if (Math.floorMod(entities.get(i).getIndex(), taskCount) == taskIndex) {
                    action.accept(entities.get(i));
                }
            }
        });
    }
}
