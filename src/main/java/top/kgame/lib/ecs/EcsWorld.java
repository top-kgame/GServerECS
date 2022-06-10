package top.kgame.lib.ecs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.command.EcsCommand;
import top.kgame.lib.ecs.command.EcsCommandBuffer;
import top.kgame.lib.ecs.core.*;
import top.kgame.lib.ecs.tools.EcsClassScanner;

import java.util.Collection;

/**
 * 非线程安全，只能在单线程使用
 */
public class EcsWorld{
    private static final Logger logger = LogManager.getLogger(EcsWorld.class);
    private static final int INIT_LOGIC_TIME = -1;

    private State state = State.INIT;
    private long currentTime = INIT_LOGIC_TIME;

    private final EcsEntityManager entityManager = new EcsEntityManager(this);
    private EcsEntity[] waitDestroyEntity = new EcsEntity[16];
    private int waitDestroyEntitySize = 0;
    private EcsSystemGroup currentSystemGroup;

    private final EcsSystemManager systemManager = new EcsSystemManager(this);

    private final EcsCommandBuffer commandBuffer = new EcsCommandBuffer();;

    private Object context;

    EntityQuery findOrCreateEntityQuery(ComponentFilter componentTypes) {
        return this.entityManager.findOrCreateEntityQuery(componentTypes);
    }

    public void addDelayCommand(EcsCommand command) {
        commandBuffer.addCommand(command);
    }

    public int getComponentIndex(Class<? extends EcsComponent> type) {
        return entityManager.getComponentIndex(type);
    }

    public EcsSystemGroup getCurrentSystemGroup() {
        return this.currentSystemGroup;
    }

    public void setCurrentSystemGroup(EcsSystemGroup currentSystemGroup) {
        this.currentSystemGroup = currentSystemGroup;
    }

    private enum State {
        INIT,
        WAIT_RUNNING,
        RUNNING,
        WAIT_DESTROY,
        DESTROYING,
        DESTROYED,
    }

    EcsWorld() {}

    /**
     * 生成 EcsWorld实例
     * @param packageName 需要扫描的包名
     * @return EcsWorld 实例
     */
    public static EcsWorld generateInstance(String packageName) {
        EcsWorld ecsWorld = generateDefaultInstance();
        ecsWorld.scanPackage(packageName);
        ecsWorld.init();
        return ecsWorld;
    }

    /**
     * 生成 EcsWorld实例
     * @param packageNames 需要扫描的包名列表
     * @return EcsWorld 实例
     */
    public static EcsWorld generateInstance(String... packageNames) {
        EcsWorld ecsWorld = generateDefaultInstance();
        for (String packageName : packageNames) {
            ecsWorld.scanPackage(packageName);
        }
        ecsWorld.init();
        return ecsWorld;
    }

    private static EcsWorld generateDefaultInstance() {
        EcsWorld ecsWorld = new EcsWorld();
        ecsWorld.scanPackage(EcsWorld.class.getPackageName());
        return ecsWorld;
    }

    private void scanPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return;
        }
        packageName = packageName.trim();
        EcsClassScanner ecsClassScanner = EcsClassScanner.getInstance(packageName);
        entityManager.register(ecsClassScanner);
        systemManager.register(ecsClassScanner);
    }

    private void init() {
        systemManager.init();
        state = State.WAIT_RUNNING;
    }

    /**
     * 设置自定义上下文
     * @param context 上下文对象
     */
    public void setContext(Object context) {
        this.context = context;
    }

    /**
     * 获取自定义上下文，调用之前需要先调用setContext设置上下文
     * @param <T> 上下文类型
     * @return 上下文对象
     * @throws ClassCastException 当上下文对象无法转换为指定类型时抛出异常
     */
    @SuppressWarnings({"unchecked"})
    public <T> T getContext() {
        return (T) context;
    }
    /**
     * 关闭World。
     * 如果在update期间调用，会等本次所有System update完成之后才执行关闭逻辑。
     */
    public void close() {
        if (state == State.INIT || state == State.DESTROYED) {
            return;
        }
        if (state == State.RUNNING) {
            state = State.WAIT_DESTROY;
            return;
        }
        logger.info("Disposing ecs world at time {}...", currentTime);
        state = State.DESTROYING;
        currentTime = INIT_LOGIC_TIME;
        waitDestroyEntitySize = 0;
        systemManager.clean();
        entityManager.clean();
        commandBuffer.clear();
        state = State.DESTROYED;
    }

    public boolean isClosed() {
        return state == State.DESTROYED;
    }

    // 通过EntityFactory类型ID创建实体
    public EcsEntity createEntity(int factoryTypeId) {
        EntityFactory entityFactory = entityManager.getEntityFactory(factoryTypeId);
        if (entityFactory == null) {
            throw new IllegalArgumentException("No entity factory found for type id " + factoryTypeId);
        }
        return entityFactory.create(this.entityManager);
    }

    // 通过工厂类创建实体
    public EcsEntity createEntity(Class<? extends EntityFactory> klass) {
        EntityFactory entityFactory = entityManager.getEntityFactory(klass);
        return entityFactory == null ? null : entityFactory.create(this.entityManager);
    }

    public void requestDestroyEntity(int entityIndex) {
        EcsEntity entity = getEntity(entityIndex);
        if (entity != null) {
            requestDestroyEntity(entity);
        }
    }

    public void requestDestroyEntity(EcsEntity entity) {
        if (entity.getDestroyTime() > 0) {
            return;
        }
        entity.setDestroyTime(currentTime);
        entity.addComponent(DestroyingComponent.generate());
        if (waitDestroyEntitySize >= waitDestroyEntity.length) {
            EcsEntity[] newArray = new EcsEntity[waitDestroyEntity.length * 2];
            System.arraycopy(waitDestroyEntity, 0, newArray, 0, waitDestroyEntity.length);
            waitDestroyEntity = newArray;
        }
        waitDestroyEntity[waitDestroyEntitySize++] = entity;
    }

    public EcsEntity getEntity(int entityIndex) {
        return entityManager.getEntity(entityIndex);
    }

    public Collection<EcsEntity> getAllEntity() {
        return entityManager.getAllEntity();
    }

    /**
     * 执行ECS世界更新循环
     * <p>执行所有系统更新，处理实体销毁，执行EcsCommandScope.WORLD级的EcsCommand。</p>
     * <p>时间戳必须严格递增。</p>
     *
     * @param now 当前时间戳（毫秒），必须大于上次传入的时间
     * @throws IllegalArgumentException 当时间戳无效时抛出异常
     */
    public void update(long now) {
        if (currentTime >= now) {
            throw new IllegalArgumentException(String.format(
                "EcsWorld try update failed! reason: currentTime >= nowTime. currentTime: %d, now: %d", 
                currentTime, now));
        }
        if (state != State.WAIT_RUNNING) {
            logger.warn("EcsWorld request update failed! reason: EcsWorld has disposed");
            return;
        }
        state = State.RUNNING;
        this.currentTime = now;
        systemManager.update();
        for (int i = 0; i < waitDestroyEntitySize; i++) {
            entityManager.destroyEntity(waitDestroyEntity[i]);
        }
        waitDestroyEntitySize = 0;
        commandBuffer.execute();
        if (state == State.WAIT_DESTROY) {
            close();
        } else {
            state = State.WAIT_RUNNING;
        }
    }

    public long getCurrentTime() {
        return currentTime;
    }
}
