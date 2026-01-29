package top.kgame.lib.ecs.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {
    private static final Logger logger = LogManager.getLogger(ClassUtils.class);

    /**
     * 从包中获取指定注解的class
     * @param packageName 要扫描的包名
     * @param annoClass 注解类型
     * @return 带有指定注解的类集合
     */
    public static Set<Class<?>> getClassByAnnotation(String packageName, Class<? extends Annotation> annoClass) {
        if (packageName == null || packageName.trim().isEmpty() || annoClass == null) {
            return Collections.emptySet();
        }
        Set<Class<?>> targetClasses = new HashSet<>();
        try {
            Set<Class<?>> packageClasses = getClassesFromPackage(packageName);
            for (Class<?> klass : packageClasses) {
                Annotation targetAnnotation = klass.getAnnotation(annoClass);
                if (targetAnnotation != null) {
                    targetClasses.add(klass);
                }
            }
        } catch (IOException e) {
            logger.warn("get class by annotation failed! package: {}, annotation: {}", packageName, annoClass.getName(), e);
        }
        return targetClasses;
    }

    /**
     * 扫描指定包中的所有类
     * @param packageName 要扫描的包名.
     * @return 指定包中的所有class对象
     */
    public static Set<Class<?>> getClassesFromPackage(String packageName) throws IOException {
        if (packageName == null || packageName.trim().isEmpty()) {
            return Collections.emptySet();
        }
        
        Set<Class<?>> classes = new HashSet<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            try {
                if (resource.getProtocol().equals("jar")) {
                    // 正确处理JAR URL，直接使用JarURLConnection获取JarFile
                    JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
                    classes.addAll(getClassesFromJar(jarConnection.getJarFile(), path));
                } else {
                    // 处理文件系统路径
                    String resourcePath = resource.toURI().getPath();
                    classes.addAll(getClassesFromDirectory(resourcePath, packageName));
                }
            } catch (URISyntaxException e) {
                logger.warn("URL {} to URI failed!", resource, e);
            }
        }

        return classes;
    }

    /**
     * 从文件夹中扫描指定包中的类
     * @param directoryPath 文件夹的全路径
     * @param packageName   目标包名
     * @return  包中的所有类
     */
    private static List<Class<?>> getClassesFromDirectory(String directoryPath, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (null == files) {
            return classes;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(getClassesFromDirectory(file.getAbsolutePath(), packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String fileName = file.getName();
                String className = packageName + '.' + fileName.substring(0, fileName.length() - 6);
                try {
                    // 使用 initialize=false 避免触发静态初始化，防止某些类在静态初始化时加载不存在的依赖
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    classes.add(clazz);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    logger.debug("Failed to load class: {}", className, e);
                }
            }
        }
        return classes;
    }

    /**
     * 从jar包中扫描指定包下的类
     * @param jarFile   jar文件对象
     * @param packagePath   包路径（已转换为'/'分隔符）
     * @return 包名中的所有类
     */
    private static List<Class<?>> getClassesFromJar(JarFile jarFile, String packagePath) {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.endsWith(".class") && name.startsWith(packagePath) && name.charAt(packagePath.length()) == '/') {
                String className = name.substring(0, name.length() - 6).replace('/', '.');
                try {
                    // 使用 initialize=false 避免触发静态初始化，防止某些类在静态初始化时加载不存在的依赖
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    classes.add(clazz);
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    logger.debug("Failed to load class from jar: {}", className, e);
                }
            }
        }
        return classes;
    }

    /**
     * 从包中获取继承自指定父类的所有类
     * @param scanPath 要扫描的包路径
     * @param parentClass 父类
     * @return 继承自指定父类的类集合
     */
    public static Set<Class<?>> getClassFromParent(String scanPath, Class<?> parentClass) {
        if (scanPath == null || scanPath.trim().isEmpty() || parentClass == null) {
            return Collections.emptySet();
        }
        Set<Class<?>> targetClasses = new HashSet<>();
        try {
            Set<Class<?>> packageClasses = getClassesFromPackage(scanPath);
            for (Class<?> clazz : packageClasses) {
                if (clazz.equals(parentClass)) {
                    continue;
                }
                if (parentClass.isAssignableFrom(clazz)) {
                    targetClasses.add(clazz);
                }
            }
        } catch (IOException e) {
            logger.warn("get class from parent failed! package: {}, parent: {}", scanPath, parentClass.getName(), e);
        }
        return targetClasses;
    }
    
    /**
     * 判断类是否为抽象类或接口
     * @param clazz 要检查的类
     * @return 如果类为抽象类或接口返回true，否则返回false
     */
    public static boolean isAbstract(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return Modifier.isAbstract(clazz.getModifiers());
    }

    public static Type[] generateParameterizedType(Class<?> kclass) {
        Type genType = kclass.getGenericSuperclass();
        if (genType instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments();
        } else {
            throw new RuntimeException("generateParameterizedType execute failed! reason:GenericSuperclass not instanceof ParameterizedType");
        }
    }
}
