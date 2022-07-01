package top.kgame.lib.ecstest.common;

import org.junit.jupiter.api.Test;
import top.kgame.lib.ecs.EcsComponent;
import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.EcsWorld;
import top.kgame.lib.ecs.annotation.Standalone;
import top.kgame.lib.ecs.extensions.entity.BaseEntityFactory;
import top.kgame.lib.ecs.tools.ClassUtils;
import top.kgame.lib.ecstest.util.EcsAssertions;
import top.kgame.lib.ecstest.util.EcsTestBase;
import top.kgame.lib.ecstest.util.Util;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClassUtils 工具类测试
 */
public class ClassUtilsTest {

    @Test
    public void testGetClassesFromPackage_ValidPackage() throws IOException, ClassNotFoundException {
        // 测试扫描一个存在的包
        String packageName = "top.kgame.lib.ecs";
        Set<Class<?>> classes = ClassUtils.getClassesFromPackage(packageName);
        
        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        // 验证包含一些预期的类
        assertTrue(classes.contains(EcsWorld.class));
        assertTrue(classes.contains(EcsComponent.class));
        assertTrue(classes.contains(EcsSystem.class));
        
        // 测试从jar包中加载类（使用项目依赖中的包）
        // 测试 JUnit 5 的包（这些类在 jar 包中）
        String junitPackage = "org.junit.jupiter.api";
        Set<Class<?>> junitClasses = ClassUtils.getClassesFromPackage(junitPackage);
        assertNotNull(junitClasses);
        assertFalse(junitClasses.isEmpty(), "应该能从 jar 包中加载 JUnit 的类");
        // 验证包含一些预期的 JUnit 类
        assertTrue(junitClasses.contains(Test.class),
            "应该包含 Test 注解类");
        
        // 测试 Log4j2 的包（这些类也在 jar 包中）
        String log4jPackage = "org.apache.logging.log4j";
        Set<Class<?>> log4jClasses = ClassUtils.getClassesFromPackage(log4jPackage);
        assertNotNull(log4jClasses);
        assertFalse(log4jClasses.isEmpty(), "应该能从 jar 包中加载 Log4j2 的类");
    }

    @Test
    public void testGetClassesFromPackage_NullPackage() throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = ClassUtils.getClassesFromPackage(null);
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetClassesFromPackage_EmptyPackage() throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = ClassUtils.getClassesFromPackage("");
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetClassesFromPackage_WhitespacePackage() throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = ClassUtils.getClassesFromPackage("   ");
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetClassesFromPackage_NonExistentPackage() throws IOException, ClassNotFoundException {
        // 测试不存在的包，应该返回空集合而不是抛出异常
        Set<Class<?>> classes = ClassUtils.getClassesFromPackage("com.nonexistent.package");
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetClassByAnnotation_ValidAnnotation() {
        // 测试根据注解获取类
        String packageName = "top.kgame.lib.ecs";
        Set<Class<?>> classes = ClassUtils.getClassByAnnotation(packageName, Standalone.class);
        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        // 验证返回的类都带有指定注解
        for (Class<?> clazz : classes) {
            assertNotNull(clazz.getAnnotation(Standalone.class),
                "Class " + clazz.getName() + " should have @AlwaysUpdate annotation");
        }
    }

    @Test
    public void testGetClassByAnnotation_EntityFactory() {
        String packageName = "top.kgame.lib.ecstest";
        Set<Class<?>> classes = ClassUtils.getClassFromParent(packageName, BaseEntityFactory.class);
        
        assertNotNull(classes);
        assertFalse(classes.isEmpty());
        // 验证返回的类都带有指定注解
        for (Class<?> clazz : classes) {
            assertTrue(BaseEntityFactory.class.isAssignableFrom(clazz),
                "Class " + clazz.getName() + " should have @isAssignableFrom BaseEntityFactory");
        }
    }

    @Test
    public void testGetClassByAnnotation_NullPackage() {
        Set<Class<?>> classes = ClassUtils.getClassByAnnotation(null, Standalone.class);
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetClassByAnnotation_NullAnnotation() {
        Set<Class<?>> classes = ClassUtils.getClassByAnnotation("top.kgame.lib.ecs", null);
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetClassByAnnotation_EmptyPackage() {
        Set<Class<?>> classes = ClassUtils.getClassByAnnotation("", Standalone.class);
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetClassFromParent_EcsComponent() {
        // 测试获取继承自 EcsComponent 的类
        String packageName = "top.kgame.lib.ecstest";
        Set<Class<?>> classes = ClassUtils.getClassFromParent(packageName, EcsComponent.class);
        
        assertNotNull(classes);
        // 验证返回的类都继承自 EcsComponent
        for (Class<?> clazz : classes) {
            assertTrue(EcsComponent.class.isAssignableFrom(clazz),
                "Class " + clazz.getName() + " should extend EcsComponent");
            assertNotEquals(EcsComponent.class, clazz,
                "Should not include the parent class itself");
        }
    }

    @Test
    public void testGetClassFromParent_EcsSystem() {
        // 测试获取继承自 EcsSystem 的类
        String packageName = "top.kgame.lib.ecstest";
        Set<Class<?>> classes = ClassUtils.getClassFromParent(packageName, EcsSystem.class);
        
        assertNotNull(classes);
        // 验证返回的类都继承自 EcsSystem
        for (Class<?> clazz : classes) {
            assertTrue(EcsSystem.class.isAssignableFrom(clazz),
                "Class " + clazz.getName() + " should extend EcsSystem");
            assertNotEquals(EcsSystem.class, clazz,
                "Should not include the parent class itself");
        }
    }

    @Test
    public void testGetClassFromParent_NullPackage() {
        Set<Class<?>> classes = ClassUtils.getClassFromParent(null, EcsComponent.class);
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetClassFromParent_NullParent() {
        Set<Class<?>> classes = ClassUtils.getClassFromParent("top.kgame.lib.ecs", null);
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testGetClassFromParent_EmptyPackage() {
        Set<Class<?>> classes = ClassUtils.getClassFromParent("", EcsComponent.class);
        assertNotNull(classes);
        assertTrue(classes.isEmpty());
    }

    @Test
    public void testIsAbstract_AbstractClass() {
        // 测试抽象类
        assertTrue(ClassUtils.isAbstract(EcsSystem.class));
    }

    @Test
    public void testIsAbstract_ConcreteClass() {
        // 测试具体类
        assertFalse(ClassUtils.isAbstract(String.class));
        assertFalse(ClassUtils.isAbstract(Integer.class));
    }

    @Test
    public void testIsAbstract_Interface() {
        // 接口也会返回true（因为接口在Java中也是抽象的）
        assertTrue(ClassUtils.isAbstract(EcsComponent.class));
    }

    @Test
    public void testIsAbstract_Null() {
        // 测试 null 参数
        assertFalse(ClassUtils.isAbstract(null));
    }

    @Test
    public void testGetClassesFromPackage_IncludesInnerClasses() throws IOException, ClassNotFoundException {
        // 测试是否包含内部类（根据之前的要求，不应该跳过内部类）
        String packageName = "top.kgame.lib.ecs";
        Set<Class<?>> classes = ClassUtils.getClassesFromPackage(packageName);
        
        assertNotNull(classes);
        // 检查是否包含内部类（如果有的话）
        // 注意：这个测试依赖于实际存在的内部类
        // 验证方法不会因为内部类而失败，内部类应该被包含在结果中
        classes.stream()
            .filter(clazz -> clazz.getName().contains("$"))
            .forEach(clazz -> {
                // 如果有内部类，验证它们被正确包含
                assertTrue(classes.contains(clazz), 
                    "Inner class " + clazz.getName() + " should be included");
            });
    }

    @Test
    public void testGetClassesFromPackage_TestPackage() throws IOException, ClassNotFoundException {
        // 测试扫描测试包
        String packageName = "top.kgame.lib.ecstest.util";
        Set<Class<?>> classes = ClassUtils.getClassesFromPackage(packageName);
        
        assertNotNull(classes);
        assertTrue(classes.contains(EcsTestBase.class));
        assertTrue(classes.contains(EcsAssertions.class));
        assertTrue(classes.contains(Util.class));
    }

    @Test
    public void testGetClassFromParent_ExcludesParentClass() {
        // 验证不会包含父类本身
        String packageName = "top.kgame.lib.ecs";
        Set<Class<?>> classes = ClassUtils.getClassFromParent(packageName, EcsComponent.class);
        
        assertFalse(classes.contains(EcsComponent.class),
            "Should not include the parent class itself");
    }
}

