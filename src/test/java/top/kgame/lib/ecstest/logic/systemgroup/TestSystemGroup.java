package top.kgame.lib.ecstest.logic.systemgroup;

import top.kgame.lib.ecs.EcsSystemGroup;

/**
 * 用于测试的具体EcsSystemGroup实现。
 * <p>不声明任何@SystemGroup子System，子System完全通过addSystem动态注入，
 * 以便精确验证addSystem/removeSystem的行为。</p>
 */
public class TestSystemGroup extends EcsSystemGroup {
    @Override
    protected void onStart() {
    }

    @Override
    protected void onStop() {
    }
}
