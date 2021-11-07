package top.kgame.lib.ecs.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.EcsSystem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  System 排序器, 用于缓存排序结果。
 * 负责对 System 列表进行拓扑排序，处理 System 之间的依赖关系
 */
public class SystemOrderManager {
    private static final Logger logger = LogManager.getLogger(SystemOrderManager.class);
    private static final ConcurrentHashMap<SystemOrdererKey, SystemOrderManager> CACHE = new ConcurrentHashMap<>();

    /**
     * 对 System 列表进行排序，返回新的排序后的列表。
     * 会缓存执行后的结果，方便后续相同 System 列表直接调用
     *
     * @return 排序后的 System 列表
     */
    public static LinkedHashSet<EcsSystem> order(Set<EcsSystem> systems) {
        SystemOrdererKey key = SystemOrdererKey.fromSystems(systems);
        SystemOrderManager instance = CACHE.computeIfAbsent(key, k -> new SystemOrderManager(k.systemTypes));
        return instance.orderByCachedTypeOrder(systems);
    }

    private final List<Class<? extends EcsSystem>> orderedSystemClasses;

    private SystemOrderManager(Class<? extends EcsSystem>[] systemTypes) {
        this.orderedSystemClasses = SystemTypeSorter.sort(systemTypes);
    }

    private LinkedHashSet<EcsSystem> orderByCachedTypeOrder(Set<EcsSystem> systems) {
        Map<Class<?>, EcsSystem> classToSystem = new HashMap<>(systems.size() * 2);
        for (EcsSystem system : systems) {
            classToSystem.put(system.getClass(), system);
        }

        LinkedHashSet<EcsSystem> sortedSystems = new LinkedHashSet<>(orderedSystemClasses.size());
        for (Class<? extends EcsSystem> type : orderedSystemClasses) {
            EcsSystem system = classToSystem.get(type);
            if (system == null) {
                throw new IllegalArgumentException("SystemOrderer key mismatch: missing system instance for type: " + type.getName());
            }
            sortedSystems.add(system);
        }
        return sortedSystems;
    }

    private static final class SystemOrdererKey {
        private final Class<? extends EcsSystem>[] systemTypes;
        private final int hash;

        private SystemOrdererKey(Class<? extends EcsSystem>[] systemTypes) {
            this.systemTypes = systemTypes;
            this.hash = Arrays.hashCode(systemTypes);
        }

        static SystemOrdererKey fromSystems(Set<EcsSystem> systems) {
            @SuppressWarnings("unchecked")
            Class<? extends EcsSystem>[] types = new Class[systems.size()];

            int i = 0;
            for (EcsSystem  system : systems) {
                types[i] = system.getClass();
                i++;
            }
            Arrays.sort(types, Comparator.comparing(Class::getName));
            return new SystemOrdererKey(types);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SystemOrdererKey that)) return false;
            return Arrays.equals(systemTypes, that.systemTypes);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
