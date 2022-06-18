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
 * 双组件更新系统基类
 * <p>
 * 用于处理同时包含2个指定组件的实体的更新逻辑。
 * 
 * @param <T1> 第一个必需的组件类型
 * @param <T2> 第二个必需的组件类型
 */
public abstract class EcsTwoComponentUpdateSystem<T1 extends EcsComponent, T2 extends EcsComponent> extends EcsLogicSystem {
    private ComponentFilterParam<T1> componentMatchType1;
    private ComponentFilterParam<T2> componentMatchType2;

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<ComponentFilterParam<?>> getMatchComponent() {
        Type[] parameterizedTypes = ClassUtils.generateParameterizedType(this.getClass());
        componentMatchType1 = ComponentFilterParam.require((Class<T1>) parameterizedTypes[0]);
        componentMatchType2 = ComponentFilterParam.require((Class<T2>) parameterizedTypes[1]);

        List<ComponentFilterParam<?>> componentMatchTypes = new ArrayList<>();
        componentMatchTypes.add(componentMatchType1);
        componentMatchTypes.add(componentMatchType2);
        return componentMatchTypes;
    }

    @Override
    protected void update() {
        for (EcsEntity entity : super.getAllMatchEntity()) {
            update(entity, entity.getComponent(componentMatchType1.getType()),
                    entity.getComponent(componentMatchType2.getType()));
        }
    }

    protected abstract void update(EcsEntity entity, T1 component, T2 component1);

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
