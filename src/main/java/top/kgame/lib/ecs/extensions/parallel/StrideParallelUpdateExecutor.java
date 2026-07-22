package top.kgame.lib.ecs.extensions.parallel;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.ParallelUpdateExecutor;
import top.kgame.lib.ecs.core.ParallelUpdateExecutorManager;

import java.util.List;
import java.util.function.Consumer;

/**
 * 交错分片并行：按实体下标轮转分配给各线程——第 1 个线程处理第 1 个实体，
 * 第 2 个线程处理第 2 个实体，第 3 个线程处理第 3 个实体，以此类推；
 * 一轮结束后再从各自起点继续（即任务 {@code t} 处理下标 {@code t, t+n, t+2n, ...}）。
 */
public final class StrideParallelUpdateExecutor extends ParallelUpdateExecutor {
    public StrideParallelUpdateExecutor(ParallelUpdateExecutorManager manager) {
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
        }else {
            runParallel(taskCount, taskIndex -> {
                for (int i = taskIndex; i < size; i += taskCount) {
                    action.accept(entities.get(i));
                }
            });
        }
    }
}
