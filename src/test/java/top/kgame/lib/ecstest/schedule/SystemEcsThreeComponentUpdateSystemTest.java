package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsThreeComponentUpdateSystem;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.Util;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.Component3;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemEcsThreeComponentUpdateSystemTest extends EcsTestBase {
    private EcsEntity entity123;
    private EcsEntity entity12;

    @Test
    void testEcsThreeComponentUpdateSystem() {
        // 创建 E123 实体（包含 Component1-3，符合条件）
        entity123 = ecsWorld.createEntity(EntityIndex.E123.getId());
        // 创建 E12 实体（只包含 Component1-2，不符合条件）
        entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());

        // 记录初始状态
        ComponentLexicographic lex123 = entity123.getComponent(ComponentLexicographic.class);
        ComponentLexicographic lex12 = entity12.getComponent(ComponentLexicographic.class);

        updateWorld(0, DEFAULT_INTERVAL * 5, DEFAULT_INTERVAL);

        // 验证 E123 会执行（data 被更新）
        ComponentLexicographic lex123After = entity123.getComponent(ComponentLexicographic.class);
        assertTrue(lex123After.data.contains("ThreeComponentSystem"), "实体 E123 的 cache 应包含 ThreeComponentSystem");

        // 验证 E12 不会执行（data 未被更新）
        ComponentLexicographic lex12After = entity12.getComponent(ComponentLexicographic.class);
        assertFalse(lex12After.data.contains("ThreeComponentSystem"), "实体 E12 的 cache 不应包含 ThreeComponentSystem");
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
    }

    @SystemGroup(SysGroupDefaultLogic.class)
    public static class TestThreeComponentSystem extends EcsThreeComponentUpdateSystem<Component1, Component2, Component3> {
        @Override
        protected void update(EcsEntity entity, Component1 component1, Component2 component2, Component3 component3) {
            Util.printSystemInfo(this.getClass(), getWorld(), entity);
            // 更新 ComponentLexicographic 的 cache 字段来标记系统已执行
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            if (lex != null) {
                lex.cache += "ThreeComponentSystem";
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

