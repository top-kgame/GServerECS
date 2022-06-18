package top.kgame.lib.ecs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.core.EcsCleanable;
import top.kgame.lib.ecs.core.EcsEntityManager;
import top.kgame.lib.ecs.core.EntityArchetype;

import java.util.*;

public class EcsEntity implements EcsCleanable {
    private static final Logger logger = LogManager.getLogger(EcsEntity.class);

    private final int index;
    private final int type;
    private long destroyTime = -1;
    private final EcsEntityManager ecsEntityManager;
    private EntityArchetype archetype = EntityArchetype.EMPTY_INSTANCE;
    private final Map<Class<? extends EcsComponent>, EcsComponent> components = new HashMap<>();

    public EcsEntity(EcsEntityManager ecsEntityManager, int index, int type) {
        this.ecsEntityManager = ecsEntityManager;
        this.index = index;
        this.type = type;
    }

    public EcsEntity(EcsEntityManager ecsEntityManager, int index, int type, Collection<? extends EcsComponent> components) {
        this.ecsEntityManager = ecsEntityManager;
        this.index = index;
        this.type = type;
        for (EcsComponent component : components) {
            addComponentInstance(component);
        }
    }

    public void init() {
        this.archetype = ecsEntityManager.getOrCreateArchetype(components.keySet());
        archetype.addEntity(this);
    }

    @SuppressWarnings({"unchecked"})
    public <T extends EcsComponent> T getComponent(Class<T> componentClass) {
        EcsComponent ecsComponent = components.get(componentClass);
        if (null == ecsComponent) {
            return null;
        }
        return (T)ecsComponent;
    }

    @Override
    public void clean() {
        components.values().forEach(component -> {
            if (component instanceof EcsCleanable ecsCleanableComponent) {
                ecsCleanableComponent.clean();
            }
        });
        components.clear();
        getArchetype().removeEntity(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EcsEntity entity = (EcsEntity) o;
        return index == entity.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("EcsEntity{");
        result.append("index=").append(index);
        result.append("archetype=").append(archetype);
        result.append("data=[");
        for (EcsComponent component : components.values()) {
            if (null == component) {
                continue;
            }
            result.append(component.getClass().getSimpleName()).append(",");
        }
        result.append("]");
        return result.toString();
    }

    public EntityArchetype getArchetype() {
        return archetype;
    }

    public int getIndex() {
        return index;
    }

    public boolean hasComponent(Class<? extends EcsComponent> klass) {
        return components.containsKey(klass);
    }

    public boolean addComponent(EcsComponent component) {
        Class<? extends EcsComponent> componentClass = component.getClass();
        if (hasComponent(componentClass)) {
            logger.warn("add component failed! reason: component already exists of entity:{} componentType:{}",
                    getIndex(), componentClass.getSimpleName());
            return false;
        }
        int componentIndex = ecsEntityManager.getComponentIndex(componentClass);
        EntityArchetype oldArchetype = getArchetype();
        BitSet newBitset = oldArchetype.bitSet();
        newBitset.set(componentIndex);
        EntityArchetype newArcheType = ecsEntityManager.getArchetype(newBitset);
        if (null == newArcheType) {
            Set<Class<? extends EcsComponent>> newTypes = new HashSet<>(oldArchetype.getComponentTypes());
            newTypes.add(componentClass);
            newArcheType = ecsEntityManager.createArchetype(newBitset, newTypes);
        }
        updateArchetype(newArcheType, oldArchetype);
        this.components.put(component.getClass(), component);
        return true;
    }

    public EcsComponent removeComponent(Class<? extends EcsComponent> componentClass) {
        EntityArchetype oldArchetype = getArchetype();
        int componentIndex = ecsEntityManager.getComponentIndex(componentClass);
        BitSet newBitset = (BitSet) oldArchetype.bitSet();
        newBitset.clear(componentIndex);
        EntityArchetype newArcheType = ecsEntityManager.getArchetype(newBitset);
        if (null == newArcheType) {
            Set<Class<? extends EcsComponent>> newTypes = new HashSet<>(oldArchetype.getComponentTypes());
            newTypes.remove(componentClass);
            newArcheType = ecsEntityManager.createArchetype(newBitset, newTypes);
        }
        updateArchetype(newArcheType, oldArchetype);
        return components.remove(componentClass);
    }

    private void addComponentInstance(EcsComponent component) {
        if (this.components.containsKey(component.getClass())) {
            return;
        }
        this.components.put(component.getClass(), component);
    }

    public int getType() {
        return type;
    }

    private void updateArchetype(EntityArchetype newArchetype, EntityArchetype oldArchetype) {
        newArchetype.addEntity(this);
        oldArchetype.removeEntity(this);
        archetype = newArchetype;
    }

    public EcsWorld getEcsWorld() {
        return ecsEntityManager.getEcsWorld();
    }

    public long getDestroyTime() {
        return destroyTime;
    }

    public void setDestroyTime(long destroyTime) {
        this.destroyTime = destroyTime;
    }
}
