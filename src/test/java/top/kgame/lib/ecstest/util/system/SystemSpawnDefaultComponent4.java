package top.kgame.lib.ecstest.util.system;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsInitializeSystem;
import top.kgame.lib.ecstest.util.Util;
import top.kgame.lib.ecstest.util.component.Component4;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultSpawn;

import java.util.Collection;
import java.util.List;

@SystemGroup(SysGroupDefaultSpawn.class)
public class SystemSpawnDefaultComponent4 extends EcsInitializeSystem<Component4> {

    @Override
    public boolean onInitialize(EcsEntity entity, Component4 data) {
        Util.printSystemInfo(this.getClass(), getWorld(), entity);
        ComponentLexicographic lexicographic = entity.getComponent(ComponentLexicographic.class);
        assert lexicographic != null;
        lexicographic.cache += "o" + data.data;
        return true;
    }

    @Override
    public Collection<Class<? extends EcsComponent>> getExtraRequirementComponent() {
        return List.of(ComponentLexicographic.class);
    }

    @Override
    public Collection<Class<? extends EcsComponent>> getExtraExcludeComponent() {
        return List.of();
    }

    @Override
    protected SystemInitFinishSingle getInitFinishSingle() {
        return new SystemInitFinishSingle() {
        };
    }
}
