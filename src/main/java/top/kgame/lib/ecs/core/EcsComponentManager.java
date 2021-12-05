package top.kgame.lib.ecs.core;

import top.kgame.lib.ecs.exception.NoDefaultConstructorException;
import top.kgame.lib.ecs.tools.EcsClassScanner;
import top.kgame.lib.ecs.tools.EcsUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EcsComponentManager {
    private static final int INVALID_INDEX = -1;

    private final Map<Class<? extends EcsComponent>, Integer> classMap = new HashMap<>();
    private final Map<Integer, Class<? extends EcsComponent>> indextMap = new HashMap<>();
    private int componentIndex = 0;

    public void register(EcsClassScanner ecsClassScanner) {
        for (Class<? extends EcsComponent> componentClass : ecsClassScanner.getComponentClasses()) {
            classMap.computeIfAbsent(componentClass, clazz -> {
               int index = componentIndex++;
                indextMap.put(index, clazz);
               return index;
            });
        }
    }

    public int getComponentIndex(Class<? extends EcsComponent> componentClass) {
        return classMap.getOrDefault(componentClass, INVALID_INDEX);
    }

    public BitSet generateBitSet(Collection<Class<? extends EcsComponent>> components) {
        if (null == components || components.isEmpty()) {
            return EcsUtils.EMPTY_BITSET;
        }

        BitSet bitSet = new BitSet();
        for (Class<? extends EcsComponent> componentClass : components) {
            int index = getComponentIndex(componentClass);
            bitSet.set(index);
        }
        return bitSet;
    }

    public  <T extends EcsComponent>  T createComponent(Class<T> c) {
        try {
            return c.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new NoDefaultConstructorException(c);
        }
    }
}
