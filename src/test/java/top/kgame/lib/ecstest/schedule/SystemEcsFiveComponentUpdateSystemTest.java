package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsFiveComponentUpdateSystem;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.Util;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.Component4;
import top.kgame.lib.ecstest.util.component.Component5;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemEcsFiveComponentUpdateSystemTest extends EcsTestBase {
    private EcsEntity entity12345;
    private EcsEntity entity123;

    @Test
    void testEcsFiveComponentUpdateSystem() {
        // 创建 E12345 实体（包含 Component1-5，符合条件）
        entity12345 = ecsWorld.createEntity(EntityIndex.E123.getId());
        entity12345.addComponent(new Component4());
        entity12345.addComponent(new Component5());

        // 创建 E123 实体（只包含 Component1-3，不符合条件）
        entity123 = ecsWorld.createEntity(EntityIndex.E12.getId());
        entity123.addComponent(new Component3());

        // 记录初始状态
        ComponentLexicographic lex12345 = entity12345.getComponent(ComponentLexicographic.class);
        ComponentLexicographic lex123 = entity123.getComponent(ComponentLexicographic.class);

        updateWorld(0, DEFAULT_INTERVAL * 5, DEFAULT_INTERVAL);

        // 验证 E12345 会执行（cache 被更新）
        ComponentLexicographic lex12345After = entity12345.getComponent(ComponentLexicographic.class);
        assertTrue(lex12345After.data.contains("FiveComponentSystem"), "实体 E12345 的 cache 应包含 FiveComponentSystem");

        // 验证 E123 不会执行（cache 未被更新）
        ComponentLexicographic lex123After = entity123.getComponent(ComponentLexicographic.class);
        assertFalse(lex123After.data.contains("FiveComponentSystem"), "实体 E123 的 cache 不应包含 FiveComponentSystem");
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
    }

    @SystemGroup(SysGroupDefaultLogic.class)
    public static class TestFiveComponentSystem extends EcsFiveComponentUpdateSystem<Component1, Component2, Component3, Component4, Component5> {
        @Override
        protected void update(EcsEntity entity, Component1 c1, Component2 c2, Component3 c3, Component4 c4, Component5 c5) {
            Util.printSystemInfo(this.getClass(), getWorld(), entity);
            // 更新 ComponentLexicographic 的 cache 字段来标记系统已执行
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            if (lex != null) {
                lex.cache += "FiveComponentSystem";
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

