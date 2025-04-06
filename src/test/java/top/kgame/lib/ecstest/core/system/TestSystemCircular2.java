package top.kgame.lib.ecstest.core.system;

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

    protected void update() {
    }

    @Override
    protected void onStop() {
    }

    @Override
    protected void onDestroy() {
    }
}

