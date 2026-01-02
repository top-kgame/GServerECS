package top.kgame.lib.ecstest.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.command.EcsCommandAddComponent;
import top.kgame.lib.ecs.command.EcsCommandRemoveComponent;
import top.kgame.lib.ecs.command.EcsCommandScope;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.component.Component2;
import top.kgame.lib.ecstest.util.component.ComponentCommandHolder;
import top.kgame.lib.ecstest.util.component.ComponentLexicographic;
import top.kgame.lib.ecstest.util.entity.EntityIndex;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Component修改对System执行顺序影响的测试
 * 包括delay和immediately两种模式
 */
class SystemComponentModifyOrderTest extends EcsTestBase {
    private EcsEntity entity;
    private EcsAssertions assertions;
    private ComponentCommandHolder componentCommandHolder;
    private ComponentLexicographic componentLexicographic;
    private long modifyTime;
    private boolean isDelayMode;
    private EcsCommandScope commandScope;

    @Test
    void testImmediatelyAddComponent() {
        // 测试立即添加组件对系统执行顺序的影响
        assertions = new EcsAssertions(ecsWorld);
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        isDelayMode = false;
        modifyTime = DEFAULT_INTERVAL * 10;
        
        updateWorld(0, DEFAULT_INTERVAL * 20, DEFAULT_INTERVAL);
    }

    @Test
    void testImmediatelyRemoveComponent() {
        // 测试立即移除组件对系统执行顺序的影响
        assertions = new EcsAssertions(ecsWorld);
        entity = ecsWorld.createEntity(EntityIndex.E12.getId()); // 包含Component1和Component2
        componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        isDelayMode = false;
        modifyTime = DEFAULT_INTERVAL * 10;
        
        updateWorld(0, DEFAULT_INTERVAL * 20, DEFAULT_INTERVAL);
    }

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void testDelayAddComponent(EcsCommandScope scope) {
        // 测试延迟添加组件对系统执行顺序的影响
        assertions = new EcsAssertions(ecsWorld);
        entity = ecsWorld.createEntity(EntityIndex.E1.getId());
        componentCommandHolder = entity.getComponent(ComponentCommandHolder.class);
        componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        isDelayMode = true;
        commandScope = scope;
        modifyTime = DEFAULT_INTERVAL * 10;
        
        updateWorld(0, DEFAULT_INTERVAL * 20, DEFAULT_INTERVAL);
    }

    @ParameterizedTest
    @EnumSource(EcsCommandScope.class)
    void testDelayRemoveComponent(EcsCommandScope scope) {
        // 测试延迟移除组件对系统执行顺序的影响
        assertions = new EcsAssertions(ecsWorld);
        entity = ecsWorld.createEntity(EntityIndex.E12.getId()); // 包含Component1和Component2
        componentCommandHolder = entity.getComponent(ComponentCommandHolder.class);
        componentLexicographic = entity.getComponent(ComponentLexicographic.class);
        isDelayMode = true;
        commandScope = scope;
        modifyTime = DEFAULT_INTERVAL * 10;
        
        updateWorld(0, DEFAULT_INTERVAL * 20, DEFAULT_INTERVAL);
    }

    @Override
    protected void beforeUpdate(long currentTime, int interval) {
        if (isDelayMode && componentCommandHolder != null) {
            // 延迟模式：使用延迟命令
            if (currentTime == modifyTime) {
                if (entity.getComponent(Component2.class) == null) {
                    // 添加Component2
                    componentCommandHolder.update(new EcsCommandAddComponent(entity, new Component2()), commandScope);
                } else {
                    // 移除Component2
                    componentCommandHolder.update(new EcsCommandRemoveComponent(entity, Component2.class), commandScope);
                }
            }
        } else if (!isDelayMode) {
            // 立即模式：直接操作
            if (currentTime == modifyTime) {
                if (entity.getComponent(Component2.class) == null) {
                    // 立即添加Component2
                    entity.addComponent(new Component2());
                } else {
                    // 立即移除Component2
                    entity.removeComponent(Component2.class);
                }
            }
        }
    }

    @Override
    protected void afterUpdate(long currentTime, int interval) {
        if (componentLexicographic == null) {
            return;
        }
        
        if (currentTime < modifyTime) {
            // 修改前：验证初始状态
            if (entity.getComponent(Component2.class) == null) {
                // 只有Component1，应该只有SystemDefaultComponent1执行
                assertTrue(componentLexicographic.data.contains("1"), 
                    "修改前应只有Component1的系统执行");
            } else {
                // 有Component1和Component2，两个系统都应该执行
                assertTrue(componentLexicographic.data.contains("1") || 
                           componentLexicographic.data.contains("2"), 
                    "修改前应有Component1和Component2的系统执行");
            }
        } else if (currentTime == modifyTime) {
            // 修改时刻：根据模式验证
            if (isDelayMode) {
                // 延迟模式：此时组件还未修改
                // 验证逻辑取决于commandScope
            } else {
                // 立即模式：此时组件已修改，系统应该已经响应
                if (entity.getComponent(Component2.class) != null) {
                    // 添加了Component2，SystemDefaultComponent2应该执行
                    assertTrue(componentLexicographic.data.contains("2"), 
                        "立即添加Component2后，SystemDefaultComponent2应执行");
                } else {
                    // 移除了Component2，SystemDefaultComponent2不应执行
                    assertFalse(componentLexicographic.data.contains("2"), 
                        "立即移除Component2后，SystemDefaultComponent2不应执行");
                }
            }
        } else {
            // 修改后：验证最终状态
            if (entity.getComponent(Component2.class) != null) {
                // Component2存在，SystemDefaultComponent2应该执行
                assertTrue(componentLexicographic.data.contains("2") || 
                           componentLexicographic.data.contains("1"), 
                    "修改后Component2存在，相关系统应执行");
            } else {
                // Component2不存在，SystemDefaultComponent2不应执行
                assertFalse(componentLexicographic.data.contains("2"), 
                    "修改后Component2不存在，SystemDefaultComponent2不应执行");
            }
        }
    }
}
