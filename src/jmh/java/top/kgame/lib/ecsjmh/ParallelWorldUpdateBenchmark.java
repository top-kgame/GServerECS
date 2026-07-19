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
import top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared.SimplePerfEntityFactory;

import java.util.concurrent.TimeUnit;

/**
 * 核心路径 JMH：简单逻辑 + ParallelUpdate 下的 {@link EcsWorld#update}。
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
@State(Scope.Benchmark)
public class ParallelWorldUpdateBenchmark {

    private static final String SHARED_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.shared";
    private static final String SIMPLE_BENCH_PACKAGE =
            "top.kgame.lib.ecstest.performance.isolated.parallelupdate.bench";

    @Param({"2000", "8000"})
    public int entityCount;

    private EcsWorld world;
    private long timeCursor;

    @Setup(Level.Trial)
    public void setUp() {
        world = EcsWorld.generateInstance(SHARED_PACKAGE, SIMPLE_BENCH_PACKAGE);
        for (int i = 0; i < entityCount; i++) {
            world.createEntity(SimplePerfEntityFactory.class);
        }
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
