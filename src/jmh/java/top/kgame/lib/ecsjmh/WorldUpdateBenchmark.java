package top.kgame.lib.ecsjmh;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecstest.logic.util.entity.EntityIndex;

import java.util.concurrent.TimeUnit;

/**
 * 核心路径 JMH：稳态实体下 {@link EcsWorld#update} 平均耗时。
 * <p>
 * 仅通过 {@code mvn -Pjmh test-compile exec:exec} 或脚本 {@code --profile core} 触发。
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
@State(Scope.Benchmark)
public class WorldUpdateBenchmark {

    @Param({"1000", "10000"})
    public int entityCount;

    private EcsWorld world;
    private long timeCursor;

    @Setup(Level.Trial)
    public void setUp() {
        world = EcsWorld.generateInstance("top.kgame.lib.ecstest.logic.util");
        for (int i = 0; i < entityCount; i++) {
            world.createEntity(EntityIndex.E1.getId());
        }
        // 预热若干帧，降低首批 measurement 的冷路径影响
        for (int i = 0; i < 200; i++) {
            timeCursor += 33L;
            world.update(timeCursor);
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (world != null && !world.isClosed()) {
            world.close();
        }
    }

    @Benchmark
    public void update() {
        timeCursor += 33L;
        world.update(timeCursor);
    }
}
