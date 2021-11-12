package top.kgame.lib.ecs.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.tools.SystemOrderManager;

import java.util.Collection;
import java.util.LinkedHashSet;

public class SystemScheduler implements EcsCleanable {
    private static final Logger logger = LogManager.getLogger(SystemScheduler.class);

    private boolean sorted = false;
    private LinkedHashSet<EcsSystem> systems = new LinkedHashSet<>();

    public Collection<EcsSystem> getSortedSystem() {
        trySortSystem();
        return systems;
    }

    public void updateSystems() {
        if (!sorted) {
            sortSystem();
        }
        for (EcsSystem system : systems) {
            system.update();
        }
    }

    public void trySortSystem() {
        if (sorted) {
            return;
        }
        sortSystem();
    }

    private void sortSystem() {
        sorted = true;
        systems = SystemOrderManager.order(systems);
    }

    @Override
    public void clean() {
        for (EcsSystem system : systems) {
            system.clean();
        }
        systems.clear();
        sorted = true;
    }

    public void addSystem(EcsSystem system) {
        if (systems.contains(system)) {
            logger.warn("SortableSystemList addSystem failed! reason: {} already exist!", system.getClass().getSimpleName());
            return;
        }
        systems.add(system);
        sorted = false;
    }

    public void removeSystem(EcsSystem system) {
        if (!systems.contains(system)) {
            logger.warn("SortableSystemList removeSystem failed! reason: {} not exist!", system.getClass().getSimpleName());
            return;
        }
        systems.remove(system);
        sorted = false;
    }

    @Override
    public String toString() {
        return "SortableSystemList{" +
                "needSortSystem=" + sorted +
                ", systems=" + systems +
                '}';
    }

}
