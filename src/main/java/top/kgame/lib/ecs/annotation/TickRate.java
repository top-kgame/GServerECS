package top.kgame.lib.ecs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记系统更新间隔时间,单位毫秒
 * 
 * <p>被此注解标记的系统将在指定时间间隔后执行更新。</p>
 * <p>未被此注解标记的系统，每次更新周期都会执行。</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TickRate {
    int value();
}
