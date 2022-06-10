package top.kgame.lib.ecs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.annotation.Standalone;
import top.kgame.lib.ecs.core.SystemScheduler;

import java.util.ArrayList;
import java.util.List;

@Standalone
public abstract class EcsSystemGroup extends EcsSystem {
    private static final Logger logger = LogManager.getLogger(EcsSystemGroup.class);

    private final SystemScheduler systemScheduler = new SystemScheduler();
    protected final List<EcsSystem> systemsToRemove = new ArrayList<>();

    @Override
    public void onInit() {
        for (EcsSystem childSystem : super.ecsSystemManager.getSystemInGroup(this)) {
            systemScheduler.addSystem(childSystem);
        }
        systemScheduler.trySortSystem();
        logger.info("{} order: {}", this.getClass().getSimpleName(), systemScheduler);
    }

    public void addSystem(EcsSystem system) {
        systemScheduler.addSystem(system);
        logger.info("{} add system:{} ", this.getClass().getSimpleName(), system.getClass());
    }

    public void removeSystem(EcsSystem system) {
        systemsToRemove.add(system);
        logger.info("{} remove system:{}", this.getClass().getSimpleName(), system.getClass());
    }

    @Override
    public void update() {
        getWorld().setCurrentSystemGroup(this);
        systemScheduler.updateSystems();
        destroyRemovedSystem();
        getWorld().setCurrentSystemGroup(null);
    }

    private void destroyRemovedSystem() {
        if (!systemsToRemove.isEmpty()) {
            for(EcsSystem system : systemsToRemove) {
                systemScheduler.removeSystem(system);
                system.clean();
            }
            systemsToRemove.clear();
        }
    }

    @Override
    protected void onDestroy() {
        destroyRemovedSystem();
        systemScheduler.clean();
    }
}
