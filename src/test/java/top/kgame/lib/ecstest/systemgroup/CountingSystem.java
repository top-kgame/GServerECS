package top.kgame.lib.ecstest.systemgroup;

import top.kgame.lib.ecs.EcsSystem;

/**
 * 用于测试的可观测System。
 * <p>记录各生命周期方法（onStart/update/onStop/onDestroy）的调用次数，
 * 方便测试用例断言EcsSystemGroup对子System的调度行为。</p>
 */
public abstract class CountingSystem extends EcsSystem {
    public int initCount = 0;
    public int startCount = 0;
    public int updateCount = 0;
    public int stopCount = 0;
    public int destroyCount = 0;

    /** 控制needUpdate返回值，默认true表示每次都会执行update。 */
    public boolean shouldUpdate = true;

    @Override
    protected void onInit() {
        initCount++;
    }

    @Override
    protected void onStart() {
        startCount++;
    }

    @Override
    protected boolean needUpdate() {
        return shouldUpdate;
    }

    @Override
    protected void update() {
        updateCount++;
    }

    @Override
    protected void onStop() {
        stopCount++;
    }

    @Override
    protected void onDestroy() {
        destroyCount++;
    }
}
