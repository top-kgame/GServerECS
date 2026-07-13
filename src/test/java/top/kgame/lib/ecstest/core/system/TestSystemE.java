package top.kgame.lib.ecstest.core.system;

import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.annotation.After;

/**
 * 测试系统E - 在D之后执行
 */
@After(value = {TestSystemD.class})
public class TestSystemE extends EcsSystem {
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

