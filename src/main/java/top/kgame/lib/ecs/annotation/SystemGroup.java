package top.kgame.lib.ecs.annotation;

import top.kgame.lib.ecs.EcsSystemGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记EcsSystem在指定EcsSystemGroup中执行更新
 * 
 * <p>被此注解标记的EcsSystem将在指定EcsSystemGroup中执行更新。</p>
 * <p>未被此注解标记的EcsSystem，属于和EcsSystemGroup同级的顶层系统。由EcsWorld调度。</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SystemGroup {
    Class<? extends EcsSystemGroup> value();
}
