package top.kgame.lib.ecs.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.EcsSystemGroup;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.exception.NoDefaultConstructorException;
import top.kgame.lib.ecs.tools.EcsClassScanner;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class EcsSystemManager implements EcsCleanable {
    private static final Logger logger = LogManager.getLogger(EcsSystemManager.class);

    private final EcsWorld world;
    private final SystemScheduler topLevelSystemScheduler = new SystemScheduler();
    private final Set<Class<? extends EcsSystem>> topSystemClasses = new HashSet<>();
    private final Map<Class<? extends EcsSystemGroup>, Set<Class<? extends EcsSystem>>> groupChildMap = new HashMap<>();

    public EcsSystemManager(final EcsWorld world) {
        this.world = world;
    }

    public void register(EcsClassScanner ecsClassScanner) {
        ecsClassScanner.getGroupChildTypeMap().forEach((group, childTypeSet) -> {
            groupChildMap.computeIfAbsent(group, key -> new HashSet<>())
                    .addAll(childTypeSet);
        });

        topSystemClasses.addAll(ecsClassScanner.getTopSystemClasses());
    }
    public void init() {
        for (Class<? extends EcsSystem> systemClz : topSystemClasses) {
            EcsSystem system = createSystem(systemClz);
            topLevelSystemScheduler.addSystem(system);
        }
        topLevelSystemScheduler.getSortedSystem();
        logger.info("{} order: {}", this.getClass().getSimpleName(), topLevelSystemScheduler);
    }

    public <T extends EcsSystem> T createSystem(Class<T> systemClass) {
        T system;
        try {
            system = systemClass.getConstructor().newInstance();
            system.init(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new NoDefaultConstructorException(systemClass);
        }
        return system;
    }

    public EcsWorld getWorld() {
        return world;
    }

    @Override
    public void clean() {
        topLevelSystemScheduler.clean();
    }

    public void update() {
        topLevelSystemScheduler.updateSystems();
    }

    public Set<EcsSystem> getSystemInGroup(EcsSystemGroup ecsSystemGroup) {
        Set<EcsSystem> systemInGroup = new HashSet<>();
        for (Class<? extends EcsSystem> childSystemClass : groupChildMap.getOrDefault(ecsSystemGroup.getClass(), Collections.emptySet())) {
            EcsSystem system = createSystem(childSystemClass);
            if (null == system) {
                logger.error("addSystemToUpdateList failed! reason: generate EcsSystem failed! systemClass:{}", childSystemClass.getSimpleName());
                continue;
            }
            systemInGroup.add(system);
        }
        return systemInGroup;
    }
}
