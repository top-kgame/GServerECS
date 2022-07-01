package top.kgame.lib.ecstest.util;

import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;

public class Util {
    public static void printSystemInfo(Class<?> sysClass, EcsWorld ecsWorld, EcsEntity entity) {
        System.out.println(sysClass.getSimpleName() + " update at: " + ecsWorld.getCurrentTime() + " entity: " + entity.getIndex());
    }
}
