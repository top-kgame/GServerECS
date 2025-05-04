package top.kgame.lib.ecstest.component.add.immediately;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.After;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsOneComponentUpdateSystem;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;
import top.kgame.lib.ecstest.util.system.SystemDefaultComponent2;

@SystemGroup(SysGroupDefaultLogic.class)
@After(value = SystemDefaultComponent2.class)
public class EcsSystemImmediatelyAdd extends EcsOneComponentUpdateSystem<Component1> {
    @Override
    protected void update(EcsEntity entity, Component1 component) {
        EcsComponentAddInSystemTest.Context context = getWorld().getContext();
        if (null == context) {
            return;
        }
        if (getWorld().getCurrentTime() == context.addComponentTime) {
            entity.addComponent(new Component3());
        }
    }
}
