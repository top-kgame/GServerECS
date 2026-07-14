package top.kgame.lib.ecstest.logic.core.system;

import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.annotation.After;
import top.kgame.lib.ecstest.logic.util.system.SystemDefaultComponent1;

/**
 * 测试系统 - 用于测试跨组依赖（依赖不在同一组的系统）
 */
@After(value = {SystemDefaultComponent1.class})
public class TestSystemCrossGroup extends EcsSystem {
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

