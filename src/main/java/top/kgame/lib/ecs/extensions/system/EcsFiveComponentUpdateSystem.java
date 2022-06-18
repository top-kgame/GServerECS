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
 * 五组件更新系统基类
 * <p>
 * 用于处理同时包含5个指定组件的实体的更新逻辑。
 * 
 * @param <T1> 第一个必需的组件类型
 * @param <T2> 第二个必需的组件类型
 * @param <T3> 第三个必需的组件类型
 * @param <T4> 第四个必需的组件类型
 * @param <T5> 第五个必需的组件类型
 */
public abstract class EcsFiveComponentUpdateSystem<T1 extends EcsComponent,
        T2 extends EcsComponent, T3 extends EcsComponent, T4 extends EcsComponent, T5 extends EcsComponent> extends EcsLogicSystem {

    private ComponentFilterParam<T1> componentMatchType1;
    private ComponentFilterParam<T2> componentMatchType2;
    private ComponentFilterParam<T3> componentMatchType3;
    private ComponentFilterParam<T4> componentMatchType4;
    private ComponentFilterParam<T5> componentMatchType5;

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<ComponentFilterParam<?>> getMatchComponent() {
        Type[] parameterizedTypes = ClassUtils.generateParameterizedType(this.getClass());
        componentMatchType1 = ComponentFilterParam.require((Class<T1>) parameterizedTypes[0]);
        componentMatchType2 = ComponentFilterParam.require((Class<T2>) parameterizedTypes[1]);
        componentMatchType3 = ComponentFilterParam.require((Class<T3>) parameterizedTypes[2]);
        componentMatchType4 = ComponentFilterParam.require((Class<T4>) parameterizedTypes[3]);
        componentMatchType5 = ComponentFilterParam.require((Class<T5>) parameterizedTypes[4]);

        List<ComponentFilterParam<?>> componentMatchTypes = new ArrayList<>();
        componentMatchTypes.add(componentMatchType1);
        componentMatchTypes.add(componentMatchType2);
        componentMatchTypes.add(componentMatchType3);
        componentMatchTypes.add(componentMatchType4);
        componentMatchTypes.add(componentMatchType5);
        return componentMatchTypes;
    }

    @Override
    protected void update() {
        for (EcsEntity entity : super.getAllMatchEntity()) {
            update(entity, entity.getComponent(componentMatchType1.getType()),
                    entity.getComponent(componentMatchType2.getType()),
                    entity.getComponent(componentMatchType3.getType()),
                    entity.getComponent(componentMatchType4.getType()),
                    entity.getComponent(componentMatchType5.getType()));
        }
    }

    protected abstract void update(EcsEntity entity, T1 component, T2 component1, T3 component2, T4 component3, T5 component5);

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
