package top.kgame.lib.ecs;

import top.kgame.lib.ecs.annotation.Standalone;
import top.kgame.lib.ecs.annotation.TickRate;
import top.kgame.lib.ecs.command.EcsCommand;
import top.kgame.lib.ecs.command.EcsCommandBuffer;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecs.core.ComponentFilter;
import top.kgame.lib.ecs.core.EcsCleanable;
import top.kgame.lib.ecs.core.EcsSystemManager;
import top.kgame.lib.ecs.core.EntityQuery;
import top.kgame.lib.ecs.exception.InvalidEcsSystemState;
import top.kgame.lib.ecs.exception.UnsupportedCommandException;

import java.util.Collection;
import java.util.Collections;

public abstract class EcsSystem implements EcsCleanable {
    private EcsWorld ecsWorld;
    protected EcsSystemManager ecsSystemManager;
    private final EcsCommandBuffer commandBuffer = new EcsCommandBuffer();

    private boolean standalone = false;
    private boolean hasInit = false;
    private boolean started = false;
    private boolean destroyed = false;
    private EntityQuery entityQuery;
    private int updateInterval = 0;
    private long nextUpdateTime = Long.MIN_VALUE;

    public void tryUpdate() {
        if (!hasInit) {
            throw new InvalidEcsSystemState("can't update system before init");
        }
        if (ecsWorld.getCurrentTime() < nextUpdateTime) {
            return;
        }
        if (standalone || hasMatchEntity()) {
            run();
        } else {
            tryStop();
        }
        nextUpdateTime = ecsWorld.getCurrentTime() + updateInterval;
    }

    private void run() {
        if (!started) {
            started = true;
            onStart();
        }
        update();
        commandBuffer.execute();
    }

    private boolean hasMatchEntity() {
        if (entityQuery == null) {
            return false;
        }
        return !entityQuery.isEmpty();
    }

    private void tryStop() {
        if (started) {
            started = false;
            onStop();
        }
    }

    @Override
    public void clean() {
        if (destroyed) {
            return;
        }
        if (started) {
            started = false;
            onStop();
        }
        if (!destroyed) {
            onDestroy();
            destroyed = true;
        }
        commandBuffer.clear();
    }

    public void init(EcsSystemManager systemManager) {
        this.ecsWorld = systemManager.getWorld();
        this.ecsSystemManager = systemManager;
        Standalone standaloneAnno = this.getClass().getAnnotation(Standalone.class);
        if (standaloneAnno != null) {
            standalone = true;
        }
        TickRate timeIntervalAnno = this.getClass().getAnnotation(TickRate.class);
        if (null != timeIntervalAnno) {
            this.updateInterval = timeIntervalAnno.value();
        }
        destroyed = false;
        started = false;
        onInit();
        hasInit = true;
    }

    protected void registerEntityFilter(ComponentFilter componentTypes) {
        if (entityQuery == null) {
            entityQuery = ecsWorld.findOrCreateEntityQuery(componentTypes);
            return;
        }
        if (!entityQuery.matchFilter(componentTypes)) {
            throw new UnsupportedOperationException("Repeatedly setting EntityQuery");
        }
    }

    protected Collection<EcsEntity> getAllMatchEntity() {
        if (entityQuery == null) {
            return Collections.emptyList();
        }
        return entityQuery.getEntityList();
    }

    public EcsWorld getWorld() {
        return ecsWorld;
    }

    public void addDelayCommand(EcsCommand command, EcsCommandScope level) {
        switch (level) {
            case SYSTEM -> this.commandBuffer.addCommand(command);
            case SYSTEM_GROUP -> {
                if (this instanceof EcsSystemGroup) {
                    this.commandBuffer.addCommand(command);
                } else {
                    EcsSystemGroup currentSystemGroup = ecsWorld.getCurrentSystemGroup();
                    if (currentSystemGroup != null) {
                        currentSystemGroup.addDelayCommand(command, level);
                    } else {
                        throw new UnsupportedCommandException("EcsCommandScope.SYSTEM_GROUP only support the system which in EcsSystemGroups");
                    }
                }
            }
            case WORLD -> ecsWorld.addDelayCommand(command);
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    protected abstract void onInit();

    /**
     * 在停止状态下：
     * <p>如果alwaysUpdateSystem是true 或者 存在匹配的Entity时执行该方法<p/>
     * <p>该方法执行后System会被置为启动状态<p/>
     * <p>该方法在System的生命周期内有可能被多次执行<p/>
     */
    protected abstract void onStart();

    protected abstract void update();

    /**
     * 在启动状态下：
     * <p>如果没有@Standalone注解，且不存在匹配的Entity时，执行该方法<p/>
     * <p>该方法执行后System会被置为停止状态<p/>
     * <p>该方法在System的生命周期内有可能被多次执行<p/>
     */
    protected abstract void onStop();

    /**
     * System销毁时执行该方法。
     * <p>该方法在System的生命周期内只会执行一次<p/>
     */
    protected abstract void onDestroy();
}