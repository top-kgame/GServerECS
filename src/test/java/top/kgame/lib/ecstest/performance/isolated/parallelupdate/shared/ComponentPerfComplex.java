package top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared;

import top.kgame.lib.ecs.EcsComponent;

/**
 * 复杂逻辑性能测试组件：每帧对缓冲做可观计算。
 */
public class ComponentPerfComplex implements EcsComponent {
    public static final int BUFFER_SIZE = 256;

    public final double[] data = new double[BUFFER_SIZE];
    public double accum;

    public ComponentPerfComplex() {
        for (int i = 0; i < data.length; i++) {
            data[i] = i * 0.01 + 1.0;
        }
    }
}
