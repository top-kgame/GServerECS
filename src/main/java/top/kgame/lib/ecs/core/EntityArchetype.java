package top.kgame.lib.ecs.core;


import top.kgame.lib.ecs.EcsEntity;

import java.util.*;

public class EntityArchetype implements EcsCleanable {
    public static final BitSet EMPTY_BITSET = new BitSet();
    public static final EntityArchetype EMPTY_INSTANCE = new EMPTY(EMPTY_BITSET, Collections.emptySet());
    private final Set<Class<? extends EcsComponent>> componentMatchTypes;
    private final List<EcsEntity> entityList = new ArrayList<>();
    private final BitSet bitSet;

    private EntityArchetype(BitSet bitSet, Collection<Class<? extends EcsComponent>> componentMatchTypes) {
        this.componentMatchTypes = new HashSet<>(componentMatchTypes);
        this.bitSet = bitSet;
    }

    public static EntityArchetype newInstance(BitSet bitSet, Collection<Class<? extends EcsComponent>> components) {
        return new EntityArchetype(bitSet, components);
    }

    public boolean isSubset(BitSet subset) {
        for (int i = subset.nextSetBit(0); i >= 0; i = subset.nextSetBit(i + 1)) {
            if (!bitSet.get(i)) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(BitSet bs) {
        return bitSet.intersects(bs);
    }

    private static class EMPTY extends EntityArchetype {
        private EMPTY(BitSet bitSet, Set<Class<? extends EcsComponent>> componentMatchTypes) {
            super(bitSet, componentMatchTypes);
        }

        @Override
        public void addEntity(EcsEntity entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeEntity(EcsEntity entity) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityArchetype that = (EntityArchetype) o;
        return componentMatchTypes.equals(that.componentMatchTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentMatchTypes);
    }

    @Override
    public void clean() {
        componentMatchTypes.clear();
        entityList.clear();
    }

    public Set<Class<? extends EcsComponent>> getComponentTypes() {
        return componentMatchTypes;
    }

    public List<EcsEntity> getEntityList() {
        return entityList;
    }

    public int entityCount() {
        return entityList.size();
    }

    public boolean hasComponent(Class<? extends EcsComponent> componentClass) {
        return componentMatchTypes.contains(componentClass);
    }

    public void addEntity(EcsEntity entity) {
        entityList.add(entity);
    }

    public void removeEntity(EcsEntity entity) {
        entityList.remove(entity);
    }

    public BitSet bitSet() {
        return (BitSet)bitSet.clone();
    }
}
