package top.kgame.lib.ecs.extensions.system;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.core.ComponentFilterParam;
import top.kgame.lib.ecs.exception.InvalidSystemInitFinishSingle;
import top.kgame.lib.ecs.tools.ClassUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 实体初始化系统基类
 * <p>
 * 自动处理包含指定组件的实体初始化，为每个实体添加SystemState组件标记初始化完成。
 * 
 * @param <T> 实体初始化所需的组件类型
 */
public abstract class EcsInitializeSystem<T extends EcsComponent> extends EcsLogicSystem {
    public static abstract class SystemInitFinishSingle implements EcsComponent {}

    private ComponentFilterParam<T> matchComponentMatchType;
    private SystemInitFinishSingle systemInitFinishSingle;

    public EcsInitializeSystem() {}

    @Override
    protected void onInit() {
        systemInitFinishSingle = getInitFinishSingle();
        if (systemInitFinishSingle == null) {
            throw new InvalidSystemInitFinishSingle("getInitFinishSingle() result is null");
        }
        super.onInit();
    }

    @Override
    protected void update() {
        Collection<EcsEntity> entityList = super.getAllMatchEntity();
        for (EcsEntity entity : entityList) {
            if (onInitialize(entity, entity.getComponent(matchComponentMatchType.getType()))) {
                entity.addComponent(systemInitFinishSingle);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<ComponentFilterParam<?>> getMatchComponent() {
        Type[] parameterizedTypes = ClassUtils.generateParameterizedType(this.getClass());
        matchComponentMatchType = ComponentFilterParam.require((Class<T>) parameterizedTypes[0]);

        List<ComponentFilterParam<?>> typeList = new ArrayList<>();
        typeList.add(matchComponentMatchType);
        typeList.add(ComponentFilterParam.exclude(systemInitFinishSingle.getClass()));
        return typeList;
    }

    @Override
    protected void onStart() {}

    @Override
    protected void onStop() {}

    @Override
    protected void onDestroy() {}

    public abstract boolean onInitialize(EcsEntity entity, T data);

    protected abstract SystemInitFinishSingle getInitFinishSingle();
}
