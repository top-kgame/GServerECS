package top.kgame.lib.ecs.extensions.system;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.ComponentFilterParam;
import top.kgame.lib.ecs.tools.ClassUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 单组件更新系统基类
 * <p>
 * 用于处理包含单个指定组件的实体的更新逻辑。
 * 
 * @param <T> 实体更新处理所需的组件类型
 */
public abstract class EcsOneComponentUpdateSystem<T extends EcsComponent> extends EcsLogicSystem {
    private ComponentFilterParam<T> matchComponentMatchType;

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<ComponentFilterParam<?>> getMatchComponent() {
        Type[] parameterizedTypes = ClassUtils.generateParameterizedType(this.getClass());
        matchComponentMatchType = ComponentFilterParam.require((Class<T>) parameterizedTypes[0]);

        List<ComponentFilterParam<?>> typeList = new ArrayList<>();
        typeList.add(matchComponentMatchType);
        return typeList;
    }

    @Override
    protected void update() {
        Collection<EcsEntity> entities = super.getAllMatchEntity();
        for (EcsEntity entity : entities) {
            update(entity, entity.getComponent(matchComponentMatchType.getType()));
        }
    }

    protected abstract void update(EcsEntity entity, T component);

    @Override
    public Collection<Class<? extends EcsComponent>> getExtraRequirementComponent() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Class<? extends EcsComponent>> getExtraExcludeComponent() {
        return Collections.emptyList();
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onDestroy() {

    }
}
