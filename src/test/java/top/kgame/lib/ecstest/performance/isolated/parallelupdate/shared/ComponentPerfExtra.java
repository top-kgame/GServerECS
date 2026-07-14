package top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared;

import top.kgame.lib.ecs.EcsComponent;

/**
 * 多系统/多组件性能测试用的第二组件。
 */
public class ComponentPerfExtra implements EcsComponent {
    public long value;
    public int tick;
}
