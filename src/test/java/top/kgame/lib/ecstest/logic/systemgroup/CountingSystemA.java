package top.kgame.lib.ecstest.logic.systemgroup;

/**
 * 无依赖的可观测子System，用于EcsSystemGroup的addSystem/removeSystem测试。
 * <p>注意：需要为每个测试用的子System单独定义类型，因为SystemScheduler排序时以类型为key。</p>
 */
public class CountingSystemA extends CountingSystem {
}
