package top.kgame.lib.ecstest.core.system;

import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.annotation.After;
import top.kgame.lib.ecs.annotation.Before;

/**
 * 测试系统D - 在A之后，在B之前执行
 */
@After(value = {TestSystemA.class})
@Before(value = {TestSystemB.class})
public class TestSystemD extends EcsSystem {
    @Override
    protected void onInit() {
    }

    @Override
    protected void onStart() {
    }

    protected void update() {
    }

    @Override
    protected void onStop() {
    }

    @Override
    protected void onDestroy() {
    }
}

