package top.kgame.lib.ecs.annotation;

import java.lang.annotation.*;

/**
 * 标记EcsSystem始终执行更新，无论是否有匹配的实体
 * 
 * <p>被此注解标记的EcsSystem将在每个更新周期中执行，即使没有实体包含该EcsSystem所需的组件。</p>
 * <p>没有被此注解标记的EcsSystem，在每个更新周期中，只有在有实体包含该EcsSystem所需的组件时，才会执行更新。</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Standalone {
}
