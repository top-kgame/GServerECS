package top.kgame.lib.ecstest.util;

import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsEntity;
import top.kgame.lib.ecs.EcsWorld;

import java.lang.reflect.Field;

/**
 * ECS断言工具类
 * 提供静态断言方法，封装常见的断言逻辑
 */
public class EcsAssertions {
    private final EcsWorld ecsWorld;
    
    public EcsAssertions(EcsWorld ecsWorld) {
        this.ecsWorld = ecsWorld;
    }
    
    /**
     * 断言实体存在
     * 触发时机：在测试的每个时间点，需要验证实体是否存在时调用
     * 使用场景：
     *   1. 在实体应该存在的时间段内验证实体存在
     *   2. 在实体被移除前验证实体仍然存在
     * 示例：
     *   if (currentTime < removeTime) {
     *       assertions.assertEntityExists(entity, currentTime);
     *   }
     */
    public void assertEntityExists(EcsEntity entity, long currentTime) {
        EcsEntity found = ecsWorld.getEntity(entity.getIndex());
        assert found == entity : "EcsEntity should exist at time " + currentTime;
    }
    
    /**
     * 断言实体不存在
     * 触发时机：在实体应该被移除后验证实体不存在时调用
     * 使用场景：
     *   1. 在实体被移除后验证实体已不存在
     *   2. 在延迟移除操作生效后验证实体已被销毁
     * 示例：
     *   if (currentTime >= removeTime) {
     *       assertions.assertEntityNotExists(entity, currentTime);
     *   }
     */
    public void assertEntityNotExists(EcsEntity entity, long currentTime) {
        EcsEntity found = ecsWorld.getEntity(entity.getIndex());
        assert found == null : "EcsEntity should not exist at time " + currentTime;
    }
    
    /**
     * 断言组件的更新时间
     * @param entity 实体
     * @param componentClass 组件类型
     * @param expectedTime 期望的更新时间
     */
    public <T extends EcsComponent> void assertComponentUpdateTime(EcsEntity entity, Class<T> componentClass, long expectedTime) {
        T component = entity.getComponent(componentClass);
        assert component != null : "Component " + componentClass.getSimpleName() + " should exist";
        try {
            Field updateTimeField = componentClass.getField("updateTime");
            long actualTime = updateTimeField.getLong(component);
            assert actualTime == expectedTime : 
                "Component " + componentClass.getSimpleName() + 
                " updateTime should be " + expectedTime + " but was " + actualTime;
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Component " + componentClass.getSimpleName() + " does not have updateTime field", e);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Failed to access updateTime field", e);
        }
    }
    
    /**
     * 断言组件的生成时间
     * @param entity 实体
     * @param componentClass 组件类型
     * @param expectedTime 期望的生成时间
     */
    public <T extends EcsComponent> void assertComponentSpawnTime(EcsEntity entity, Class<T> componentClass, long expectedTime) {
        T component = entity.getComponent(componentClass);
        assert component != null : "Component " + componentClass.getSimpleName() + " should exist";
        try {
            Field spawnTimeField = componentClass.getField("spawnTime");
            long actualTime = spawnTimeField.getLong(component);
            assert actualTime == expectedTime : 
                "Component " + componentClass.getSimpleName() + 
                " spawnTime should be " + expectedTime + " but was " + actualTime;
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Component " + componentClass.getSimpleName() + " does not have spawnTime field", e);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Failed to access spawnTime field", e);
        }
    }
    
    /**
     * 断言字符串字段的值
     * @param entity 实体
     * @param componentClass 组件类型
     * @param fieldName 字段名
     * @param expectedValue 期望的值
     */
    public <T extends EcsComponent> void assertComponentField(EcsEntity entity, Class<T> componentClass, String fieldName, String expectedValue) {
        T component = entity.getComponent(componentClass);
        assert component != null : "Component " + componentClass.getSimpleName() + " should exist";
        try {
            Field field = componentClass.getField(fieldName);
            String actualValue = (String) field.get(component);
            assert expectedValue.equals(actualValue) : 
                "Component " + componentClass.getSimpleName() + 
                " field " + fieldName + " should be \"" + expectedValue + 
                "\" but was \"" + actualValue + "\"";
        } catch (NoSuchFieldException e) {
            throw new AssertionError("Component " + componentClass.getSimpleName() + " does not have field " + fieldName, e);
        } catch (IllegalAccessException e) {
            throw new AssertionError("Failed to access field " + fieldName, e);
        }
    }
    
    /**
     * 流畅API：组件断言构建器
     */
    public <T extends EcsComponent> ComponentAssertion<T> assertComponent(EcsEntity entity, Class<T> componentClass) {
        return new ComponentAssertion<>(this, entity, componentClass);
    }
    
    public static class ComponentAssertion<T extends EcsComponent> {
        private final EcsAssertions assertions;
        private final EcsEntity entity;
        private final Class<T> componentClass;
        
        ComponentAssertion(EcsAssertions assertions, EcsEntity entity, Class<T> componentClass) {
            this.assertions = assertions;
            this.entity = entity;
            this.componentClass = componentClass;
        }
        
        public ComponentAssertion<T> hasUpdateTime(long expectedTime) {
            assertions.assertComponentUpdateTime(entity, componentClass, expectedTime);
            return this;
        }
        
        public ComponentAssertion<T> hasSpawnTime(long expectedTime) {
            assertions.assertComponentSpawnTime(entity, componentClass, expectedTime);
            return this;
        }
        
        public ComponentAssertion<T> hasField(String fieldName, String expectedValue) {
            assertions.assertComponentField(entity, componentClass, fieldName, expectedValue);
            return this;
        }
    }
}

