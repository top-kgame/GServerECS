package top.kgame.lib.ecs.extensions.system;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.ComponentFilterParam;
import top.kgame.lib.ecs.extensions.component.DestroyingComponent;
import top.kgame.lib.ecs.tools.ClassUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 实体销毁系统基类
 * <p>
 * 自动处理包含指定组件和DestroyingComponent的实体销毁逻辑。
 * 
 * @param <T> 销毁处理所需的组件类型
 */
public abstract class EcsDestroySystem<T extends EcsComponent> extends EcsLogicSystem {
    private ComponentFilterParam<T> matchComponentMatchType;

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<ComponentFilterParam<?>> getMatchComponent() {
        Type[] parameterizedTypes = ClassUtils.generateParameterizedType(this.getClass());
        matchComponentMatchType = ComponentFilterParam.require((Class<T>) parameterizedTypes[0]);

        List<ComponentFilterParam<?>> typeList = new ArrayList<>();
        typeList.add(matchComponentMatchType);
        typeList.add(ComponentFilterParam.require(DestroyingComponent.class));
        return typeList;
    }

    @Override
    protected void update() {
        Collection<EcsEntity> entities = super.getAllMatchEntity();
        for (EcsEntity entity : entities) {
            onEntityDestroy(entity, entity.getComponent(matchComponentMatchType.getType()));
        }
    }

    protected abstract void onEntityDestroy(EcsEntity entity, T component);

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
