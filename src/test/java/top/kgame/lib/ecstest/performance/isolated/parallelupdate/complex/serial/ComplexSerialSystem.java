package top.kgame.lib.ecstest.performance.isolated.parallelupdate.complex.serial;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.ComponentPerfComplex;

public class ComplexSerialSystem extends EcsOneComponentUpdateSystem<ComponentPerfComplex> {
    @Override
    protected void update(EcsEntity entity, ComponentPerfComplex component) {
        double[] data = component.data;
        double sum = 0;
        for (int i = 0; i < data.length; i++) {
            double v = data[i];
            sum += Math.sin(v) * Math.cos(v * 0.5) + v * v;
            data[i] = sum * 0.0001 + v * 0.999;
        }
        component.accum = sum;
    }
}
