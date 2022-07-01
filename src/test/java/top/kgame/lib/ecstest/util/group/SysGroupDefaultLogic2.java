package top.kgame.lib.ecstest.util.group;

import top.kgame.lib.ecs.EcsSystemGroup;
import top.kgame.lib.ecs.annotation.After;

@After(value = { SysGroupDefaultSpawn.class })
public class SysGroupDefaultLogic2 extends EcsSystemGroup {
    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }
}
