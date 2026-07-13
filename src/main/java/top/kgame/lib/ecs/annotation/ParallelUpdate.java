package top.kgame.lib.ecs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 EcsLogicSystem 内对匹配实体的 update 可以多线程并行执行。
 *
 * <p>被此注解标记的系统，在单次 update 中会以多线程方式遍历并处理匹配的实体。
 * 系统之间仍按原有顺序串行执行，仅系统内部的实体处理可并行。</p>
 *
 * <p>此注解只能用于 EcsLogicSystem 及其子类，不能用于 EcsSystemGroup、EcsStandaloneUpdateSystem 等。</p>
 *
 * <p>标注此注解的系统，其 update 实现必须是线程安全的，且不应在并行处理期间直接修改实体结构
 * （如添加/移除组件、销毁实体等），如需修改必须通过 EcsCommand 执行。</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ParallelUpdate {
}
