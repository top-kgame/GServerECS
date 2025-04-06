package top.kgame.lib.ecstest.core.system;

import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.annotation.After;

/**
 * 测试系统F - 在E之后执行
 */
@After(value = {TestSystemE.class})
public class TestSystemF extends EcsSystem {
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

