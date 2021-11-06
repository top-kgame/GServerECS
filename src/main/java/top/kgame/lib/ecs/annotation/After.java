package top.kgame.lib.ecs.annotation;

import top.kgame.lib.ecs.EcsSystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记EcsSystem在指定EcsSystem之后执行更新
 * 
 * <p>被此注解标记的EcsSystem将在指定EcsSystem执行完成之后执行更新。</p>
 * <p>相同条件的EcsSystem，会按照字典序执行。</p>
 * <p>可用于SystemGroup</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface After {
    Class<? extends EcsSystem>[] value();
}
