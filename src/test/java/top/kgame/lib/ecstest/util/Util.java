package top.kgame.lib.ecstest.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;

public class Util {
    private final static Logger log = LogManager.getLogger(Util.class);
    public static void printSystemInfo(Class<?> sysClass, EcsWorld ecsWorld, EcsEntity entity) {
        log.debug("{} update at: {} entity: {}", sysClass.getSimpleName(), ecsWorld.getCurrentTime(), entity.getIndex());
    }
}
