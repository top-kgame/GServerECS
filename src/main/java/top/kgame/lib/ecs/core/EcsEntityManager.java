package top.kgame.lib.ecs.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.exception.InvalidEcsEntityFactoryException;
import top.kgame.lib.ecs.exception.NoDefaultConstructorException;
import top.kgame.lib.ecs.tools.EcsClassScanner;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class EcsEntityManager implements EcsCleanable {
    private static final Logger logger = LogManager.getLogger(EcsEntityManager.class);

    private final EcsWorld ecsWorld;
    private final Map<BitSet, EntityArchetype> entityArchetypes = new HashMap<>();
    private final List<EntityQuery> entityQueries = new ArrayList<>();
    private final Map<Integer, EcsEntity> entityIndex = new HashMap<>();
    private final EntityFactoryIndex entityFactoryIndex = new EntityFactoryIndex();
    private final EcsComponentManager componentManager = new EcsComponentManager();

    private int entitiesNextIndex = 1;

    public EcsEntityManager(final EcsWorld ecsWorld) {
        this.ecsWorld = ecsWorld;
    }

    @Override
    public void clean() {
        entityArchetypes.values().forEach(EntityArchetype::clean);
        entityArchetypes.clear();
        entityQueries.forEach(EntityQuery::clean);
        entityQueries.clear();
        entityIndex.clear();
        entityFactoryIndex.clear();
    }

    public void register(EcsClassScanner ecsClassScanner) {
        ecsClassScanner.getEntityFactoryClass().forEach(entityFactoryClass -> {
            try {
                EntityFactory entityFactory = entityFactoryClass.getDeclaredConstructor().newInstance();
                entityFactoryIndex.registerEntityFactory(entityFactory);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new NoDefaultConstructorException(entityFactoryClass);
            }
        });
        componentManager.register(ecsClassScanner);
    }

    public EcsEntity getEntity(int index) {
        return entityIndex.get(index);
    }

    public Collection<EcsEntity> getAllEntity() {
        return entityIndex.values();
    }

    public int getComponentIndex(Class<? extends EcsComponent> type) {
        return componentManager.getComponentIndex(type);
    }

    public EntityArchetype getArchetype(BitSet newBitset) {
        return  entityArchetypes.get(newBitset);
    }

    private static class EntityFactoryIndex {
        private final Map<Integer, EntityFactory> entityFactoryTypeIdIndex = new HashMap<>();
        private final Map<Class<? extends EntityFactory>, EntityFactory> entityFactoryClassIndex = new HashMap<>();

        private void registerEntityFactory(EntityFactory entityFactory) {
            if (entityFactory.typeId() == 0) {
                throw new InvalidEcsEntityFactoryException("Invalid Factory, typeId must not be 0 !");
            }

            if (entityFactoryTypeIdIndex.containsKey(entityFactory.typeId())) {
                logger.error("EntityFactory already exist. name:{} typeId:{}", entityFactory.getClass().getName(), entityFactory.typeId());
            }

            entityFactoryTypeIdIndex.put(entityFactory.typeId(), entityFactory);
            entityFactoryClassIndex.put(entityFactory.getClass(), entityFactory);
        }

        private EntityFactory get(int typeId) {
            return entityFactoryTypeIdIndex.get(typeId);
        }

        private EntityFactory get(Class<? extends EntityFactory> clazz) {
            return entityFactoryClassIndex.get(clazz);
        }

        private void clear() {
            entityFactoryTypeIdIndex.clear();
            entityFactoryClassIndex.clear();
        }
    }


    public EntityFactory getEntityFactory(int factoryType) {
        return entityFactoryIndex.get(factoryType);
    }
    public EntityFactory getEntityFactory(Class<? extends EntityFactory> clazz) {
        return entityFactoryIndex.get(clazz);
    }

    public EcsEntity createEntityInstance(int typeId, Collection<? extends EcsComponent> components) {
        EcsEntity entity = new EcsEntity(this, entitiesNextIndex++, typeId, components);
        entity.init();
        entityIndex.put(entity.getIndex(), entity);
        return entity;
    }

    public EntityArchetype getOrCreateArchetype(Collection<Class<? extends EcsComponent>> components) {
        BitSet bitSet = componentManager.generateBitSet(components);
        EntityArchetype existArchetype = entityArchetypes.get(bitSet);
        if (existArchetype != null) {
            return existArchetype;
        }
        return createArchetype(bitSet, components);
    }

    public EntityArchetype createArchetype(BitSet bitSet, Collection<Class<? extends EcsComponent>> components) {
        EntityArchetype entityArchetype = EntityArchetype.newInstance(bitSet, components);
        entityArchetypes.put(entityArchetype.bitSet(), entityArchetype);
        entityQueries.forEach(entityQuery -> entityQuery.tryAddArchetype(entityArchetype));
        return entityArchetype;
    }

    private boolean notExistEntity(EcsEntity entity) {
        if (entitiesNextIndex < entity.getIndex()) {
            return true;
        }
        return !entityIndex.containsKey(entity.getIndex());
    }

    public EntityQuery findOrCreateEntityQuery(ComponentFilter componentFilter) {
        EntityQuery entityQuery = null;
        for (EntityQuery item : entityQueries) {
            if (item.matchFilter(componentFilter)) {
                entityQuery = item;
            }
        }
        if (entityQuery == null) {
            EntityQuery newEntityQuery = new EntityQuery(componentFilter);
            newEntityQuery.tryAddArchetype(entityArchetypes.values());
            entityQueries.add(newEntityQuery);
            return newEntityQuery;
        } else {
            return entityQuery;
        }
    }

    public EcsWorld getEcsWorld() {
        return ecsWorld;
    }

    public void destroyEntity(EcsEntity entity) {
        if (notExistEntity(entity)) {
            logger.warn("destroy entity failed! reason: entity not exist. index:{}", entity.getIndex());
            return;
        }
        if (entityIndex.remove(entity.getIndex()) != null) {
            entity.clean();
        }
    }
}
