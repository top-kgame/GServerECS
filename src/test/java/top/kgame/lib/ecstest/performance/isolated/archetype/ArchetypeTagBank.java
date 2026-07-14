package top.kgame.lib.ecstest.performance.isolated.archetype;

import top.kgame.lib.ecs.EcsComponent;

/**
 * 提供多个独立 Tag 组件类型，用于构造大量唯一 Archetype。
 */
public final class ArchetypeTagBank {
    private ArchetypeTagBank() {
    }

    public static class T0 implements EcsComponent {
    }

    public static class T1 implements EcsComponent {
    }

    public static class T2 implements EcsComponent {
    }

    public static class T3 implements EcsComponent {
    }

    public static class T4 implements EcsComponent {
    }

    public static class T5 implements EcsComponent {
    }

    @SuppressWarnings("unchecked")
    public static final Class<? extends EcsComponent>[] TAGS = new Class[]{
            T0.class, T1.class, T2.class, T3.class, T4.class, T5.class
    };

    public static EcsComponent newTag(int index) {
        return switch (index) {
            case 0 -> new T0();
            case 1 -> new T1();
            case 2 -> new T2();
            case 3 -> new T3();
            case 4 -> new T4();
            case 5 -> new T5();
            default -> throw new IllegalArgumentException("tag index out of range: " + index);
        };
    }
}
