package top.kgame.lib.ecs.command;

/**
 * 命令作用域
 * 
 * <p>SYSTEM: 系统作用域，表示命令在当前EcsSystem执行完成之后执行。</p>
 * <p>SYSTEM_GROUP: 系统组作用域，表示命令在当前EcsSystemGroup执行完成之后执行。</p>
 * <p>WORLD: 世界作用域，表示命令在本次EcsWorld update完成之后执行。</p>
 */
public enum EcsCommandScope {
    SYSTEM,
    SYSTEM_GROUP,
    WORLD,
}
