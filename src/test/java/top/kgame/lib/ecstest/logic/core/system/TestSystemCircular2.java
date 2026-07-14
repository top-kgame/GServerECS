package top.kgame.lib.ecstest.logic.core.system;

import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.annotation.After;

/**
 * 测试系统 - 用于测试环形依赖（Circular1 -> Circular2 -> Circular1）
 */
@After(value = {TestSystemCircular1.class})
public class TestSystemCircular2 extends EcsSystem {
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

