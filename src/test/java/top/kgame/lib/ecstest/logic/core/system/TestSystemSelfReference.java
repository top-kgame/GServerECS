package top.kgame.lib.ecstest.logic.core.system;

import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.annotation.After;
import top.kgame.lib.ecs.annotation.Before;

/**
 * 测试系统 - 自引用（UpdateBeforeSystem和UpdateAfterSystem都指向自己）
 */
@After(value = {TestSystemSelfReference.class})
@Before(value = {TestSystemSelfReference.class})
public class TestSystemSelfReference extends EcsSystem {
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

