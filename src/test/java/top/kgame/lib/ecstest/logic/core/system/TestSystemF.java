package top.kgame.lib.ecstest.logic.core.system;

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

    @Override
    protected boolean needUpdate() {
        return true;
    }

    @Override
    protected void update() {
    }

    @Override
    protected void onStop() {
    }

    @Override
    protected void onDestroy() {
    }
}

