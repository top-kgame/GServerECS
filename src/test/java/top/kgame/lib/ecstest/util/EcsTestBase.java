package top.kgame.lib.ecstest.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import top.kgame.lib.ecs.EcsWorld;

/**
 * ECS测试基类
 * 提供通用的测试基础设施，包括世界初始化和更新循环
 */
public abstract class EcsTestBase {
    protected static final Logger log = LogManager.getLogger(EcsTestBase.class);
    protected EcsWorld ecsWorld;
    protected static final int DEFAULT_INTERVAL = 33;
    protected static final int DEFAULT_DURATION = 100; // 100个interval
    
    @BeforeEach
    protected void setUp() {
        log.info("Setting up {}...", this.getClass().getSimpleName());
        String packageName = this.getClass().getPackage().getName();
        ecsWorld = EcsWorld.generateInstance(packageName, "top.kgame.lib.ecstest.util");
    }

    /**
     * 通用的更新循环方法,使用默认tick频率
     * 作用：将"循环更新ECS世界"的逻辑提取出来，通过抽象方法让子类定义具体行为
     * 执行流程：
     *   1. 调用 beforeUpdate() - 在更新前执行（如设置命令、准备数据等）
     *   2. 调用 ecsWorld.update() - 更新ECS世界
     *   3. 调用 afterUpdate() - 在更新后执行（如断言检查等）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     */
    protected final void updateWorld(long startTime, long endTime) {
        updateWorld(startTime, endTime, DEFAULT_DURATION);
    }
    /**
     * 通用的更新循环方法
     * 作用：将"循环更新ECS世界"的逻辑提取出来，通过抽象方法让子类定义具体行为
     * 执行流程：
     *   1. 调用 beforeUpdate() - 在更新前执行（如设置命令、准备数据等）
     *   2. 调用 ecsWorld.update() - 更新ECS世界
     *   3. 调用 afterUpdate() - 在更新后执行（如断言检查等）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔
     */
    protected final void updateWorld(long startTime, long endTime, int interval) {
        while (startTime < endTime && !ecsWorld.isClosed()) {
            log.info("=====Updating world in {}=====", startTime);
            beforeUpdate(startTime, interval);  // 更新前的逻辑（抽象方法）
            ecsWorld.update(startTime);         // 更新ECS世界
            afterUpdate(startTime, interval);    // 更新后的逻辑（抽象方法）
            
            // 如果世界已关闭，停止循环
            if (ecsWorld.isClosed()) {
                break;
            }
            startTime += interval;
        }
    }
    
    /**
     * 抽象方法：在ECS世界更新前执行
     * 参数：
     *   - currentTime: 当前时间点
     *   - interval: 时间间隔
     * 使用场景：
     *   - 设置延迟命令（如延迟添加/移除组件）
     *   - 准备测试数据
     *   - 执行更新前的验证
     */
    protected abstract void beforeUpdate(long currentTime, int interval);
    
    /**
     * 抽象方法：在ECS世界更新后执行
     * 参数：
     *   - currentTime: 当前时间点
     *   - interval: 时间间隔
     * 使用场景：
     *   - 断言实体存在性
     *   - 断言组件状态
     *   - 验证更新结果
     */
    protected abstract void afterUpdate(long currentTime, int interval);
    
    /**
     * 清理资源
     */
    @AfterEach
    void tearDown() {
        if (ecsWorld != null && !ecsWorld.isClosed()) {
            ecsWorld.close();
        }
    }
}

