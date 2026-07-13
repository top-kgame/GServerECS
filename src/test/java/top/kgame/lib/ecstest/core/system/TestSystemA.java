package top.kgame.lib.ecstest.core.system;

import top.kgame.lib.ecs.EcsSystem;

/**
 * 测试系统A - 无依赖
 */
public class TestSystemA extends EcsSystem {
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

