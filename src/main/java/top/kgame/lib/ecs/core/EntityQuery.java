package top.kgame.lib.ecs.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EntityQuery implements EcsCleanable {
    private static final Logger logger = LogManager.getLogger(EntityQuery.class);
    private final ComponentFilter queryParam;
    private final List<EntityArchetype> matchArchetypes = new ArrayList<>();

    public EntityQuery(ComponentFilter componentFilter) {
        queryParam = componentFilter;
    }

    public boolean isEmpty() {
       return matchArchetypes.stream().noneMatch(
               entityArchetype -> entityArchetype.entityCount() > 0);
    }

    public int entityCount() {
        int result = 0;
        for (EntityArchetype entityArchetype : matchArchetypes) {
            result += entityArchetype.entityCount();
        }
        return result;
    }

    public List<EcsEntity> getEntityList() {
        List<EcsEntity> result = null;
        for (EntityArchetype entityArchetype : matchArchetypes) {
            if (entityArchetype.entityCount() > 0) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.addAll(entityArchetype.getEntityList());
            }
        }
        return result == null ? Collections.emptyList() : result;
    }

    public <T extends EcsComponent> List<T> getComponentDataList(Class<T> tClass) {
        List<T> result = new ArrayList<>();
        for (EntityArchetype matchEntityArchetype : matchArchetypes) {
            if (!matchEntityArchetype.hasComponent(tClass)) {
                logger.error("{} not exist in EntityQuery matchingTypes {}!", tClass.getSimpleName(), this);
                continue;
            }
            for(EcsEntity entity : matchEntityArchetype.getEntityList()) {
                EcsComponent component = entity.getComponent(tClass);
                if (null == component) {
                    logger.error("{} not exist in EcsEntity {}!", tClass.getSimpleName(), entity);
                    continue;
                }
                result.add(tClass.cast(component));
            }
        }
        return result;
    }

    public boolean matchFilter(ComponentFilter query) {
        return queryParam.equals(query);
    }

    @Override
    public void clean() {
        matchArchetypes.clear();
        queryParam.clean();
    }

    public void registerArchetype(EntityArchetype entityArchetype) {
        matchArchetypes.add(entityArchetype);
    }

    public void tryAddArchetype(EntityArchetype entityArchetype) {
        if (queryParam.isMatchingArchetype(entityArchetype)) {
            registerArchetype(entityArchetype);
        }
    }

    public void tryAddArchetype(Collection<EntityArchetype> entityArchetypes) {
        for (EntityArchetype entityArchetype : entityArchetypes) {
            tryAddArchetype(entityArchetype);
        }
    }
}
