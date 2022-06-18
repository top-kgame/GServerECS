package top.kgame.lib.ecs.extensions.system;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.ComponentFilterParam;
import top.kgame.lib.ecs.tools.ClassUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 排除单个组件的更新系统基类
 * <p>
 * 用于实现排除特定组件的实体更新系统。
 * 
 * @param <T> 要排除的组件类型
 */
public abstract class EcsExcludeComponentUpdateSystem<T extends EcsComponent> extends EcsLogicSystem {
    @SuppressWarnings("unchecked")
    @Override
    protected Collection<ComponentFilterParam<?>> getMatchComponent() {
        Type[] parameterizedTypes = ClassUtils.generateParameterizedType(this.getClass());
        ComponentFilterParam<T> matchComponentMatchType = ComponentFilterParam.exclude((Class<T>) parameterizedTypes[0]);

        List<ComponentFilterParam<?>> typeList = new ArrayList<>();
        typeList.add(matchComponentMatchType);
        return typeList;
    }

    @Override
    protected void update() {
        Collection<EcsEntity> entities = super.getAllMatchEntity();
        for (EcsEntity entity : entities) {
            update(entity);
        }
    }

    protected abstract void update(EcsEntity entity);

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
