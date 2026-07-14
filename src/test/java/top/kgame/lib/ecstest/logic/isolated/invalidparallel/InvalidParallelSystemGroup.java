package top.kgame.lib.ecstest.logic.isolated.invalidparallel;

import top.kgame.lib.ecs.EcsSystemGroup;
import top.kgame.lib.ecs.annotation.ParallelUpdate;

@ParallelUpdate
public class InvalidParallelSystemGroup extends EcsSystemGroup {
    @Override
    protected void onStart() {
    }

    @Override
    protected void onStop() {
    }
}
