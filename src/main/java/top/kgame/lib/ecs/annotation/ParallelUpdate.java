package top.kgame.lib.ecs.annotation;

import top.kgame.lib.ecs.core.ParallelUpdateExecutor;
import top.kgame.lib.ecs.core.ParallelUpdateExecutorManager;
import top.kgame.lib.ecs.extensions.parallel.RangeParallelUpdateExecutor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 EcsEntityUpdateSystem 内对匹配实体的 update 可以多线程并行执行。
 *
 * <p>被此注解标记的系统，在单次 update 中会以多线程方式遍历并处理匹配的实体。
 * 系统之间仍按原有顺序串行执行，仅系统内部的实体处理可并行。</p>
 *
 * <p>此注解只能用于 EcsEntityUpdateSystem 及其子类，不能用于 EcsSystemGroup、EcsStandaloneUpdateSystem 等。</p>
 *
 * <p>标注此注解的系统，其 update 实现必须是线程安全的，且不应在并行处理期间直接修改实体结构
 * （如添加/移除组件、销毁实体等），如需修改必须通过 EcsCommand 执行。</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ParallelUpdate {

    /**
     * 自定义并行执行器类型。默认 {@link RangeParallelUpdateExecutor}（连续区间）；
     * 交错分片可指定 {@code StrideParallelUpdateExecutor.class}。
     * <p>自定义类须有 public 构造 {@code (ParallelUpdateExecutorManager)}，或事先通过
     * {@link ParallelUpdateExecutorManager#register} 注册实例。</p>
     */
    Class<? extends ParallelUpdateExecutor> executor() default RangeParallelUpdateExecutor.class;

    /**
     * 每批至少处理的实体数；用于限制并行任务数，避免简单逻辑被调度开销淹没。
     * <p>必须 {@code > 0}；否则系统初始化时抛出异常。默认 256。</p>
     */
    int minEntityCountPerBatch() default ParallelUpdateExecutorManager.DEFAULT_MIN_ENTITIES_PER_BATCH;
}
