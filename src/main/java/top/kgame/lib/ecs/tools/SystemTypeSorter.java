package top.kgame.lib.ecs.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.kgame.lib.ecs.EcsSystem;
import top.kgame.lib.ecs.annotation.After;
import top.kgame.lib.ecs.annotation.Before;
import top.kgame.lib.ecs.exception.InvalidSystemOrderAnnotation;
import top.kgame.lib.ecs.exception.SystemSortException;

import java.util.*;

/**
 * System 类型排序器，使用拓扑排序算法对 System 类型数组进行排序。
 *
 * <h3>排序规则：</h3>
 * <ol>
 *   <li><b>依赖关系优先</b>：通过 {@link Before} 和 {@link After} 注解指定的依赖关系
 *       具有最高优先级，必须严格满足。</li>
 *   <li><b>字典序作为默认排序</b>：对于没有指定依赖关系的系统，或者多个系统同时满足执行条件时，
 *       按照类的完全限定名（FQN）的字典序进行排序。</li>
 *   <li><b>混合排序</b>：当系统中既有指定了依赖关系的系统，也有未指定依赖关系的系统时：
 *       <ul>
 *         <li>有依赖关系的系统按照依赖关系排序</li>
 *         <li>没有依赖关系的系统按照字典序排序</li>
 *         <li>在拓扑排序过程中，当多个系统同时满足执行条件（没有前置依赖）时，优先选择字典序较小的系统</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <h3>排序示例：</h3>
 * <p>假设有以下系统及其注解：</p>
 * <pre>{@code
 * // SystemA5: 在 SystemA0 之前执行
 * @Before(SystemA0.class)
 * class SystemA5 extends EcsSystem { }
 *
 * // SystemA1: 在 SystemA5 之后执行（无其他约束）
 * @After(SystemA5.class)
 * class SystemA1 extends EcsSystem { }
 *
 * // SystemA2: 在 SystemA5 之后执行（无其他约束）
 * @After(SystemA5.class)
 * class SystemA2 extends EcsSystem { }
 *
 * // SystemA3: 在 SystemA5 之后执行，但在 SystemA2 之前执行
 * @After(SystemA5.class)
 * @Before(SystemA2.class)
 * class SystemA3 extends EcsSystem { }
 *
 * // SystemA4: 在 SystemA2 之后执行，但在 SystemA0 之前执行
 * @After(SystemA2.class)
 * @Before(SystemA0.class)
 * class SystemA4 extends EcsSystem { }
 *
 * // SystemA0: 无注解，使用默认排序
 * class SystemA0 extends EcsSystem { }
 * }</pre>
 *
 * <p><b>排序结果：</b></p>
 * <ol>
 *   <li><b>SystemA5</b> - 没有前置依赖，字典序最小，首先执行</li>
 *   <li><b>SystemA1</b> - 依赖 SystemA5，且 SystemA1 的字典序小于 SystemA3</li>
 *   <li><b>SystemA3</b> - 依赖 SystemA5，且必须在 SystemA2 之前</li>
 *   <li><b>SystemA2</b> - 依赖 SystemA5，且 SystemA3 已执行</li>
 *   <li><b>SystemA4</b> - 依赖 SystemA2，且必须在 SystemA0 之前</li>
 *   <li><b>SystemA0</b> - 无依赖关系，在 SystemA5 和 SystemA4 之后执行</li>
 * </ol>
 *
 * <p><b>关键点说明：</b></p>
 * <ul>
 *   <li>SystemA5 和 SystemA0 之间：SystemA5 必须在 SystemA0 之前（通过 @UpdateBeforeSystem 指定）</li>
 *   <li>SystemA1 和 SystemA3 都依赖 SystemA5，但 SystemA1 的字典序更小，所以先执行</li>
 *   <li>SystemA3 必须在 SystemA2 之前（通过 @UpdateBeforeSystem 指定），所以即使 SystemA1 先执行，
 *       但 SystemA3 仍会在 SystemA2 之前执行</li>
 *   <li>SystemA0 虽然没有注解，但由于 SystemA5 和 SystemA4 都指定了与它的关系，所以它的位置是确定的</li>
 * </ul>
 *
 * <p><b>算法说明：</b></p>
 * <ul>
 *   <li>使用拓扑排序（Topological Sort）算法处理依赖关系</li>
 *   <li>使用优先队列（PriorityQueue）确保在多个系统同时满足条件时，按字典序选择</li>
 *   <li>检测并抛出循环依赖异常（{@link SystemSortException}）</li>
 * </ul>
 */
class SystemTypeSorter {
    private static final Logger logger = LogManager.getLogger(SystemTypeSorter.class);
    
    private final Map<Class<?>, Integer> graphIndexMap;
    private final DependencyGraphInfo[] graph;
    private final Class<? extends EcsSystem>[] needSortSystem;

    private SystemTypeSorter(Class<? extends EcsSystem>[] needSortSystem) {
        this.needSortSystem = needSortSystem;
        this.graphIndexMap = new HashMap<>(needSortSystem.length * 2);
        this.graph = new DependencyGraphInfo[needSortSystem.length];
    }

    /**
     * 对系统类型数组进行排序
     */
    static List<Class<? extends EcsSystem>> sort(Class<? extends EcsSystem>[] systemTypes) {
        SystemTypeSorter sorter = new SystemTypeSorter(systemTypes);
        sorter.buildDependencyGraph();
        return sorter.orderSystemTypes();
    }

    private void buildDependencyGraph() {
        for (int i = 0; i < needSortSystem.length; i++) {
            Class<? extends EcsSystem> type = needSortSystem[i];
            graphIndexMap.put(type, i);
            graph[i] = new DependencyGraphInfo(i, type);
        }
        for (DependencyGraphInfo dep : this.graph) {
            processBeforeDependency(dep);
            processAfterDependency(dep);
        }
    }

    private void processBeforeDependency(DependencyGraphInfo currentDependencyInfo) {
        Before beforeAnnotation = currentDependencyInfo.getSystemClass().getAnnotation(Before.class);
        if (null == beforeAnnotation) {
            return;
        }
        Class<? extends EcsSystem>[] beforeClass = beforeAnnotation.value();
        for (Class<? extends EcsSystem> dependencyClass : beforeClass) {
            if (dependencyClass.equals(currentDependencyInfo.getSystemClass())) {
                throw new InvalidSystemOrderAnnotation(currentDependencyInfo.getSystemClass() + "before itself");
            }
            int dependencyIndex = findGraphIndex(dependencyClass);
            if (dependencyIndex < 0) {
                throw new InvalidSystemOrderAnnotation(currentDependencyInfo.getSystemClass() + " before " + dependencyClass + ", but they are not in same group");
            }
            if (currentDependencyInfo.registerNextSystem(dependencyClass)) {
                graph[dependencyIndex].addPreCount();
            }
        }
    }

    private int findGraphIndex(Class<? extends EcsSystem> dependencyClass) {
        return graphIndexMap.getOrDefault(dependencyClass, -1);
    }

    /**
     * 检测并输出环形依赖
     */
    private void detectCircularDependency() {
        int n = graph.length;
        boolean[] visited = new boolean[n];
        boolean[] inStack = new boolean[n];
        List<Integer> cyclePath = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!visited[i] && dfsDetectCycle(i, visited, inStack, cyclePath)) {
                StringBuilder errorMsg = getStringBuilder(cyclePath);
                throw new SystemSortException(errorMsg.toString());
            }
        }
    }

    private boolean dfsDetectCycle(int currentIndex, boolean[] visited, boolean[] inStack, List<Integer> cyclePath) {
        visited[currentIndex] = true;
        inStack[currentIndex] = true;
        cyclePath.add(currentIndex);

        DependencyGraphInfo currentSD = graph[currentIndex];

        for (Class<? extends EcsSystem> beforeSystemClass : currentSD.getNextSystem()) {
            int beforeIndex = findGraphIndex(beforeSystemClass);
            if (beforeIndex < 0) {
                continue;
            }

            if (!visited[beforeIndex] && dfsDetectCycle(beforeIndex, visited, inStack, cyclePath)) {
                return true;
            } else if (inStack[beforeIndex]) {
                int cycleStart = cyclePath.indexOf(beforeIndex);
                if (cycleStart >= 0) {
                    cyclePath.subList(0, cycleStart).clear();
                }
                cyclePath.add(beforeIndex);
                return true;
            }
        }

        inStack[currentIndex] = false;
        cyclePath.removeLast();
        return false;
    }

    private StringBuilder getStringBuilder(List<Integer> cyclePath) {
        StringBuilder errorMsg = new StringBuilder("EcsSystem circular dependency detected in group, Cycle path: ");
        for (int idx : cyclePath) {
            errorMsg.append(graph[idx].getSystemClass().getSimpleName());
            errorMsg.append(" -> ");
        }
        errorMsg.append(graph[cyclePath.getFirst()].getSystemClass().getSimpleName());
        return errorMsg;
    }

    private void processAfterDependency(DependencyGraphInfo currentDependencyInfo) {
        After afterAnnotation = currentDependencyInfo.getSystemClass().getAnnotation(After.class);
        if (null == afterAnnotation) {
            return;
        }
        Class<? extends EcsSystem>[] afterClass = afterAnnotation.value();
        for (Class<? extends EcsSystem> dependencyClass : afterClass) {
            if (dependencyClass.equals(currentDependencyInfo.getSystemClass())) {
                throw new InvalidSystemOrderAnnotation(currentDependencyInfo.getSystemClass() + "after itself");
            }
            int dependencyIndex = findGraphIndex(dependencyClass);
            if (dependencyIndex < 0) {
                throw new InvalidSystemOrderAnnotation(currentDependencyInfo.getSystemClass() + " after " + dependencyClass + ", but they are not in same group");
            }
            if (graph[dependencyIndex].registerNextSystem(currentDependencyInfo.getSystemClass())) {
                currentDependencyInfo.addPreCount();
            }
        }
    }

    private List<Class<? extends EcsSystem>> orderSystemTypes() {
        int n = graph.length;
        int[] beforeCounts = new int[n];
        PriorityQueue<DependencyGraphInfo> systemQueue = new PriorityQueue<>();
        boolean[] processed = new boolean[n];

        for (int i = 0; i < n; i++) {
            int preCount = graph[i].getPreSystemCount();;
            beforeCounts[i] = preCount;
            if (preCount == 0) {
                systemQueue.offer(graph[i]);
            }
        }

        List<Class<? extends EcsSystem>> result = new ArrayList<>(n);
        int processedCount = 0;
        while (!systemQueue.isEmpty()) {
            DependencyGraphInfo element = systemQueue.poll();
            int systemIndex = element.getIndex();
            if (processed[systemIndex]) {
                continue;
            }
            if (beforeCounts[systemIndex] != 0) {
                continue;
            }
            processed[systemIndex] = true;
            processedCount++;
            result.add(graph[systemIndex].getSystemClass());

            for (Class<? extends EcsSystem> beforeSystemClass : graph[systemIndex].getNextSystem()) {
                int beforeSystemIndex = findGraphIndex(beforeSystemClass);
                if (beforeSystemIndex < 0) {
                    throw new SystemSortException(beforeSystemClass.getName() + " Index < 0, the value is " + beforeSystemIndex);
                }
                beforeCounts[beforeSystemIndex] -= 1;
                if (beforeCounts[beforeSystemIndex] < 0) {
                    throw new SystemSortException(beforeSystemClass.getName() + " AfterSystemCount < 0,  the value is " + beforeCounts[beforeSystemIndex]);
                }
                if (beforeCounts[beforeSystemIndex] == 0 && !processed[beforeSystemIndex]) {
                    systemQueue.offer(graph[beforeSystemIndex]);
                }
            }
        }

        if (processedCount != n) {
            detectCircularDependency();
            throw new SystemSortException("EcsSystem dependency has circular dependency! processed: " +
                    processedCount + ", total: " + n);
        }
        return result;
    }

    private static class DependencyGraphInfo implements Comparable<DependencyGraphInfo> {
        private final Class<? extends EcsSystem> systemClass;
        private final List<Class<? extends EcsSystem>> nextSystem;
        private final int index;
        private final String name;
        private int preSystemCount = 0;

        DependencyGraphInfo(int graphIndex, Class<? extends EcsSystem> systemClass) {
            this.systemClass = systemClass;
            this.nextSystem = new ArrayList<>();
            this.name = systemClass.getName();
            this.index = graphIndex;
        }

        Class<? extends EcsSystem> getSystemClass() {
            return systemClass;
        }

        boolean registerNextSystem(Class<? extends EcsSystem> dependencyClass) {
            if (nextSystem.contains(dependencyClass)) {
                return false;
            }
            nextSystem.add(dependencyClass);
            return true;
        }

        void addPreCount() {
            preSystemCount++;
        }

        List<Class<? extends EcsSystem>> getNextSystem() {
            return nextSystem;
        }

        int getPreSystemCount() {
            return preSystemCount;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        @Override
        public int compareTo(DependencyGraphInfo o) {
            return name.compareTo(o.name);
        }
    }
}
