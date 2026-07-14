package top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared;

import top.kgame.lib.ecs.EcsComponent;

/**
 * 简单逻辑性能测试组件：每帧仅做轻量算术。
 */
public class ComponentPerfSimple implements EcsComponent {
    public long value;
    public int tick;
}
