package top.kgame.lib.ecstest.util.group;

import top.kgame.lib.ecs.EcsSystemGroup;
import top.kgame.lib.ecs.annotation.After;
import top.kgame.lib.ecs.annotation.Before;

@After(value = { SysGroupDefaultSpawn.class })
@Before(value = { SysGroupDefaultDestroy.class })
public class SysGroupDefaultLogic2 extends EcsSystemGroup {
    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
