package top.kgame.lib.ecs.tools;

import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.EcsSystemGroup;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.core.EcsComponent;
import top.kgame.lib.ecs.exception.InvalidUpdateInGroupTypeException;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EcsClassScanner {
    private final Set<Class<? extends EcsSystem>> topSystemClasses = new HashSet<>();
    private final Map<Class<? extends EcsSystemGroup>, Set<Class<? extends EcsSystem>>> groupChildTypeMap = new HashMap<>();
    private final Set<Class<? extends EcsComponent>> componentClasses = new HashSet<>();

    private static final Map<String, EcsClassScanner> SCANNERS = new ConcurrentHashMap<>();
    public static EcsClassScanner getInstance(String packageName) {
        return SCANNERS.computeIfAbsent(packageName, name -> {
            EcsClassScanner newInstance = new EcsClassScanner();
            newInstance.loadPackage(packageName);
            newInstance.loadComponent(packageName);
            return newInstance;
        });
    }

    private void loadPackage(String scanPackage) {
        Set<Class<? extends EcsSystem>> ecsSystemClass = getEcsSystem(scanPackage);
        for (Class<? extends EcsSystem> clazz : ecsSystemClass) {
            if (ClassUtils.isAbstract(clazz)) {
                continue;
            }
            SystemGroup systemGroupAnnotation = clazz.getAnnotation(SystemGroup.class);
            if (systemGroupAnnotation == null) {
                topSystemClasses.add(clazz);
                continue;
            }
            Class<? extends EcsSystemGroup> groupClass = systemGroupAnnotation.value();
            if (ClassUtils.isAbstract(groupClass)
                    || !EcsSystemGroup.class.isAssignableFrom(groupClass)) {
                throw new InvalidUpdateInGroupTypeException(clazz, groupClass);
            }
            groupChildTypeMap.computeIfAbsent(groupClass, item -> new HashSet<>()).add(clazz);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadComponent(String scanPackage) {
        Set<Class<?>> componentClass =  ClassUtils.getClassFromParent(scanPackage, EcsComponent.class);
        for (Class<?> clazz : componentClass) {
            if (ClassUtils.isAbstract(clazz)) {
                continue;
            }
            componentClasses.add((Class<? extends EcsComponent>)clazz);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends EcsSystem>> getEcsSystem(String scanPackage) {
        Set<Class<?>> classes = ClassUtils.getClassFromParent(scanPackage, EcsSystem.class);
        Set<Class<? extends EcsSystem>> ecsSystemClass = new HashSet<>(classes.size() * 2);
        for (Class<?> klass : classes) {
            if (!ClassUtils.isAbstract(klass)) {
                ecsSystemClass.add((Class<? extends EcsSystem>) klass);
            }
        }
        return ecsSystemClass;
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends EcsSystem>> getEcsSystemByAnnotation(String scanPackage, Class<? extends Annotation> annoClass) {
        Set<Class<?>> classes = ClassUtils.getClassByAnnotation(scanPackage, annoClass);
        Set<Class<? extends EcsSystem>> ecsSystemClass = new HashSet<>(classes.size() * 2);
        for (Class<?> klass : classes) {
            if (!ClassUtils.isAbstract(klass) && EcsSystem.class.isAssignableFrom(klass)) {
                ecsSystemClass.add((Class<? extends EcsSystem>) klass);
            }
        }
        return ecsSystemClass;
    }


    public Map<Class<? extends EcsSystemGroup>, Set<Class<? extends EcsSystem>>> getGroupChildTypeMap() {
        return groupChildTypeMap;
    }

    public Collection<Class<? extends EcsSystem>> getTopSystemClasses() {
        return topSystemClasses;
    }

    public Set<Class<? extends EcsComponent>> getComponentClasses() {
        return componentClasses;
    }
}
