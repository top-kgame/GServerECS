package top.kgame.lib.ecs.extensions.parallel;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.ParallelUpdateExecutor;
import top.kgame.lib.ecs.core.ParallelUpdateExecutorManager;

import java.util.List;
import java.util.function.Consumer;

/**
 * 连续区间并行：将列表均分为若干连续段，每任务 for 循环处理一段 {@code [start, end)}。
 */
public final class RangeParallelUpdateExecutor extends ParallelUpdateExecutor {
    public RangeParallelUpdateExecutor(ParallelUpdateExecutorManager manager) {
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
        } else {
            super.runParallel(taskCount, taskIndex -> {
                int start = batchStart(size, taskCount, taskIndex);
                int end = batchStart(size, taskCount, taskIndex + 1);
                for (int i = start; i < end; i++) {
                    action.accept(entities.get(i));
                }
            });
        }
    }

    /** 将 [0, size) 均分为 taskCount 段时，第 taskIndex 段的起始下标。 */
    public static int batchStart(int size, int taskCount, int taskIndex) {
        return (int) ((long) size * taskIndex / taskCount);
    }

}
