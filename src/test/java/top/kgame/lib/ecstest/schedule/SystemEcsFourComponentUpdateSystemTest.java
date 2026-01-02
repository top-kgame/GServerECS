package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsFourComponentUpdateSystem;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.Util;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.Component4;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemEcsFourComponentUpdateSystemTest extends EcsTestBase {
    private EcsEntity entity1234;
    private EcsEntity entity123;

    @Test
    void testEcsFourComponentUpdateSystem() {
        // 创建 E1234 实体（包含 Component1-4，符合条件）
        entity1234 = ecsWorld.createEntity(EntityIndex.E123.getId());
        assertNotNull(entity1234, "实体 E1234 应被创建");
        entity1234.addComponent(new Component4());

        assertNotNull(entity1234.getComponent(Component1.class), "实体 E1234 应包含Component1");
        assertNotNull(entity1234.getComponent(Component2.class), "实体 E1234 应包含Component2");
        assertNotNull(entity1234.getComponent(Component3.class), "实体 E1234 应包含Component3");
        assertNotNull(entity1234.getComponent(Component4.class), "实体 E1234 应包含Component4");

        // 创建 E123 实体（只包含 Component1-3，不符合条件）
        entity123 = ecsWorld.createEntity(EntityIndex.E12.getId());
        assertNotNull(entity123, "实体 E123 应被创建");
        entity123.addComponent(new Component3());
        assertNotNull(entity123.getComponent(Component1.class), "实体 E123 应包含Component1");
        assertNotNull(entity123.getComponent(Component2.class), "实体 E123 应包含Component2");
        assertNotNull(entity123.getComponent(Component3.class), "实体 E123 应包含Component3");
        assertNull(entity123.getComponent(Component4.class), "实体 E123 不应包含Component4");

        // 记录初始状态
        ComponentLexicographic lex1234 = entity1234.getComponent(ComponentLexicographic.class);
        ComponentLexicographic lex123 = entity123.getComponent(ComponentLexicographic.class);
        assertNotNull(lex1234, "实体 E1234 应包含ComponentLexicographic");
        assertNotNull(lex123, "实体 E123 应包含ComponentLexicographic");

        updateWorld(0, DEFAULT_INTERVAL * 5, DEFAULT_INTERVAL);

        // 验证 E1234 会执行（cache 被更新）
        ComponentLexicographic lex1234After = entity1234.getComponent(ComponentLexicographic.class);
        assertNotNull(lex1234After, "实体 E1234 应包含ComponentLexicographic");
        assertTrue(lex1234After.data.contains("FourComponentSystem"), "实体 E1234 的 cache 应包含 FourComponentSystem");

        // 验证 E123 不会执行（cache 未被更新）
        ComponentLexicographic lex123After = entity123.getComponent(ComponentLexicographic.class);
        assertNotNull(lex123After, "实体 E123 应包含ComponentLexicographic");
        assertFalse(lex123After.data.contains("FourComponentSystem"), "实体 E123 的 cache 不应包含 FourComponentSystem");
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
    }

    @SystemGroup(SysGroupDefaultLogic.class)
    public static class TestFourComponentSystem extends EcsFourComponentUpdateSystem<Component1, Component2, Component3, Component4> {
        @Override
        protected void update(EcsEntity entity, Component1 c1, Component2 c2, Component3 c3, Component4 c4) {
            Util.printSystemInfo(this.getClass(), getWorld(), entity);
            // 更新 ComponentLexicographic 的 cache 字段来标记系统已执行
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            if (lex != null) {
                lex.cache += "FourComponentSystem";
            }
        }

        @Override
        public Collection<Class<? extends EcsComponent>> getExtraRequirementComponent() {
            // 要求实体必须包含 ComponentLexicographic 组件
            return List.of(ComponentLexicographic.class);
        }

        @Override
        public Collection<Class<? extends EcsComponent>> getExtraExcludeComponent() {
            return Collections.emptyList();
        }
    }
}

