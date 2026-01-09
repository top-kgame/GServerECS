package top.kgame.lib.ecstest.schedule.system;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsDestroySystem;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultDestroy;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 仅供 SystemEcsDestroySystemTest 使用的销毁系统：
 * - 销毁时修改 ComponentLexicographic.data
 * - 同时写入 world context，便于 afterUpdate 断言（避免访问已销毁 entity）
 */
@SystemGroup(SysGroupDefaultDestroy.class)
public class SystemDestroyMarkWorldContext extends EcsDestroySystem<ComponentLexicographic> {

    @Override
    protected void onEntityDestroy(EcsEntity entity, ComponentLexicographic component) {
        component.data = "destroyed";

        Object ctx = getWorld().getContext();
        if (ctx instanceof AtomicBoolean flag) {
            flag.set(true);
        }
    }

    @Override
    public Collection<Class<? extends EcsComponent>> getExtraRequirementComponent() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Class<? extends EcsComponent>> getExtraExcludeComponent() {
        return Collections.emptyList();
    }
}

