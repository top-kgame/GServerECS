package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.annotation.SystemGroup;
import top.kgame.lib.ecs.extensions.system.EcsExcludeComponentUpdateSystem;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.Util;
import top.kgame.lib.ecstest.util.component.Component1;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;
import top.kgame.lib.ecstest.util.group.SysGroupDefaultLogic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SystemEcsExcludeComponentUpdateSystemTest extends EcsTestBase {
    private EcsEntity entity1;
    private EcsEntity entity12;

    @Test
    void testEcsExcludeComponentUpdateSystem() {
        // 创建 E1 实体（只包含 Component1，不包含 Component2）
        entity1 = ecsWorld.createEntity(EntityIndex.E1.getId());

        // 创建 E12 实体（包含 Component1 和 Component2）
        entity12 = ecsWorld.createEntity(EntityIndex.E12.getId());

        // 记录初始状态
        ComponentLexicographic lex1 = entity1.getComponent(ComponentLexicographic.class);
        ComponentLexicographic lex12 = entity12.getComponent(ComponentLexicographic.class);

        updateWorld(0, DEFAULT_INTERVAL * 5, DEFAULT_INTERVAL);

        // 验证 E1 会执行（cache 被更新）
        ComponentLexicographic lex1After = entity1.getComponent(ComponentLexicographic.class);
        assertTrue(lex1After.data.contains("ExcludeComponentSystem"));

        // 验证 E12 不会执行（cache 未被更新）
        ComponentLexicographic lex12After = entity12.getComponent(ComponentLexicographic.class);
        assertFalse(lex12After.data.contains("ExcludeComponentSystem"));
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
    }

    @SystemGroup(SysGroupDefaultLogic.class)
    public static class TestExcludeComponentSystem extends EcsExcludeComponentUpdateSystem<Component2> {
        @Override
        protected void update(EcsEntity entity) {
            Util.printSystemInfo(this.getClass(), getWorld(), entity);
            // 更新 ComponentLexicographic 的 cache 字段来标记系统已执行
            ComponentLexicographic lex = entity.getComponent(ComponentLexicographic.class);
            if (lex != null) {
                lex.cache += "ExcludeComponentSystem";
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

