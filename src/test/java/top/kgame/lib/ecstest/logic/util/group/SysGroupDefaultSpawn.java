package top.kgame.lib.ecstest.logic.util.group;

import top.kgame.lib.ecs.EcsSystemGroup;
import top.kgame.lib.ecs.annotation.Before;

@Before(value = { SysGroupDefaultDestroy.class })
public class SysGroupDefaultSpawn extends EcsSystemGroup {
    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
