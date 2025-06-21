# GServerECS - Entity Component System Framework for Java Game Server

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

<div align="center">

[English](README.md) | [中文](README_CN.md)

</div>

GServerECS 是一个专为Java游戏服务器设计开发的开源ECS框架，采用 Java 语言实现。该框架提供完整的 ECS 架构支持，支持组件运行时添加/删除、系统执行序控制、实体/组件的即装即用（on-the-fly）与延迟加载（deferred）等关键特性。

本框架针对游戏服务器场景设计。一个进程可创建多个EcsWorld，每个 EcsWorld 实例可对应一个游戏房间（Room）或场景（Scene）。各 EcsWorld 被设计为线程专有（thread-confined），仅限在创建它的线程内访问，不支持跨线程调用。

如果这个项目帮到了你，欢迎点个 star⭐ 支持一下～ 这会让更多人发现它 😊

## 🌟 主要特性

### 核心功能
- **实体管理**: 高效的实体创建、销毁和生命周期管理
- **组件系统**: 支持动态添加/移除组件，组件类型安全
- **系统执行**: 灵活的系统更新机制，支持多种执行模式
- **实体原型**: 基于组件组合的实体原型系统

### 高级特性
- **系统分组**: 支持系统分组管理，便于组织复杂逻辑
- **执行顺序控制**: 通过注解精确控制系统的执行顺序
- **延迟命令**: 支持延迟执行的实体操作命令
- **实体工厂**: 工厂模式创建实体，简化实体实例化
- **自动扫描**: 基于包扫描自动发现和注册系统、组件、工厂


## 📋 系统要求

- **Java**: 21 或更高版本
- **Maven**: 3.6 或更高版本
- **依赖**: 
  - Log4j2 (2.24.3+)
  - JUnit 5 (测试)

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>top.kgame</groupId>
    <artifactId>kgame-lib-ecs</artifactId>
    <version>1.0.1</version>
</dependency>
```

### 2. 创建组件

```java
public class PositionComponent implements EcsComponent {
    public float x, y, z;
}

public class HealthComponent implements EcsComponent {
    public int currentHealth;
    public int maxHealth;
}
```

### 3. 创建系统

```java
@SystemGroup(GameSystemGroup.class) 
//未使用@SystemGroup注解的属于顶层System，和SystemGroup同一级别，都由EcsWorld直接调度
public class MovementSystem extends EcsOneComponentUpdateSystem<PositionComponent> {
    
    @Override
    protected void update(Entity entity, PositionComponent position) {
        // 更新位置逻辑
        position.x += 1.0f;
    }
}
```

### 4. 创建实体工厂

```java
// EntityFactory实现类会被自动扫描和注册，无需注解
// 只需实现EntityFactory接口或继承BaseEntityFactory即可
public class PlayerFactory extends BaseEntityFactory {

    @Override
    protected Collection<EcsComponent> generateComponent() {
      return List.of(new PositionComponent(), new HealthComponent());
    }
    
    @Override
    public int typeId() {
        return 1; // 工厂类型ID 同一EcsWorld内不可重复。
    }
}
```

### 5. 创建系统组

```java
public class GameSystemGroup extends EcsSystemGroup {
    // 系统组实现
    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {
  
    }
}
```

### 6. 使用ECS世界

```java
public class Game {
    private EcsWorld world;
    
    public void init() {
        // 创建ECS世界，指定要扫描的包名
        world = EcsWorld.generateInstance("com.example.game");
        // 可以设置自定义上下文
        world.setContext(this);
    }
    
    public void update(long currentTime) {
        // 更新ECS世界
        world.update(currentTime);
    }
    
    public void createPlayer() {
        // 通过工厂创建玩家实体
        Entity player = world.createEntity(PlayerFactory.class);
    }
    
    public void cleanup() {
        world.close();
    }
}
```
### 7. Entity相关操作

```java
// 获取组件
PositionComponent position = entity.getComponent(PositionComponent.class);

// 检查组件
if (entity.hasComponent(HealthComponent.class)) {
    // 处理逻辑
}

// 添加组件
entity.addComponent(new HealthComponent());

// 移除组件
entity.removeComponent(PositionComponent.class);

// 销毁实体
world.requestDestroyEntity(entity);
```

## 📖 注解

GServerECS提供了丰富的注解来控制系统的行为：

### 系统控制注解

#### @SystemGroup
- **作用**: 标记EcsSystem在指定EcsSystemGroup中执行更新
- **可作用对象**: EcsSystem类
- **参数**: `Class<? extends EcsSystemGroup> value()` - 系统组类型
- **说明**: 被此注解标记的EcsSystem将在指定EcsSystemGroup中执行更新。未被此注解标记的EcsSystem，属于和EcsSystemGroup同级的顶层系统，由EcsWorld调度。**注意：此注解不能用于EcsSystemGroup类，目前不支持SystemGroup的嵌套。**

#### @TickRate
- **作用**: 标记系统更新间隔时间
- **可作用对象**: EcsSystem类
- **参数**: `int value()` - 更新间隔时间（毫秒）
- **说明**: 被此注解标记的系统将在指定时间间隔后执行更新。未被此注解标记的系统，每次更新周期都会执行。

#### @Standalone
- **作用**: 标记EcsSystem始终执行更新，无论是否有匹配的实体
- **可作用对象**: EcsSystem类
- **参数**: 无
- **说明**: 被此注解标记的EcsSystem将在每个更新周期中执行，即使没有实体包含该EcsSystem所需的组件。没有被此注解标记的EcsSystem，在每个更新周期中，只有在有实体包含该EcsSystem所需的组件时，才会执行更新。

#### @After
- **作用**: 标记EcsSystem在指定同组内的EcsSystem之后执行更新
- **可作用对象**: EcsSystem类
- **参数**: `Class<? extends EcsSystem>[] value()` - 目标系统类型数组
- **说明**: 被此注解标记的EcsSystem将在指定EcsSystem执行完成之后执行更新。相同条件的EcsSystem，会按照字典序执行。可用于SystemGroup。

#### @Before
- **作用**: 标记EcsSystem在指定同组内的EcsSystem之前执行更新
- **可作用对象**: EcsSystem类
- **参数**: `Class<? extends EcsSystem>[] value()` - 目标系统类型数组
- **说明**: 被此注解标记的EcsSystem将在指定EcsSystem执行之前执行更新。相同条件的EcsSystem，会按照字典序执行。可用于SystemGroup。

### 实体工厂

EntityFactory实现类会被EcsWorld自动扫描和注册，无需注解。只需实现EntityFactory接口或继承BaseEntityFactory，框架会自动发现并注册您的工厂类。

## 🔧 预制系统类型

GServerECS提供了多种预定义的系统基类：

- `EcsOneComponentUpdateSystem<T>`: 处理单个组件的系统
- `EcsTwoComponentUpdateSystem<T1, T2>`: 处理两个组件的系统
- `EcsThreeComponentUpdateSystem<T1, T2, T3>`: 处理三个组件的系统
- `EcsFourComponentUpdateSystem<T1, T2, T3, T4>`: 处理四个组件的系统
- `EcsFiveComponentUpdateSystem<T1, T2, T3, T4, T5>`: 处理五个组件的系统
- `EcsStandaloneUpdateSystem<T>`: 处理单个组件的系统（排除其他组件）
- `EcsExcludeComponentUpdateSystem<T, E>`: 处理组件T但排除组件E的系统
- `EcsInitializeSystem<T>`: 实体初始化系统
- `EcsDestroySystem<T>`: 实体销毁系统
- `EcsLogicSystem`: 逻辑系统基类

## 📦 系统组（EcsSystemGroup）

系统组（EcsSystemGroup）是GServerECS中用于组织和管理系统执行的重要机制。系统组本身也是一个系统，可以包含多个子系统，并按照特定的顺序执行它们。

### 系统组特性

- **自动管理**: 系统组会自动扫描并管理所有使用`@SystemGroup`注解标记的系统
- **执行顺序**: 系统组内的系统会按照`@After`和`@Before`注解定义的顺序执行
- **生命周期**: 系统组具有完整的生命周期管理，包括初始化、更新和销毁
- **动态管理**: 支持在运行时添加和移除System

### 系统组层次结构

```
EcsWorld
├── 顶层系统 (未使用@SystemGroup注解)
│   ├── SystemA
│   └── SystemB
└── 系统组
    ├── GameSystemGroup
    │   ├── InputSystem
    │   ├── LogicSystem
    │   └── RenderSystem
    └── PhysicsSystemGroup
        ├── CollisionSystem
        └── MovementSystem
```

## ⚡ 延迟命令系统

GServerECS提供了完整的延迟命令系统，允许在系统执行过程中安全地执行实体和组件操作。延迟命令会在指定的作用域内执行，确保操作的原子性和一致性。

```java
public class MySystem extends EcsOneComponentUpdateSystem<MyComponent> {
    
    @Override
    protected void update(Entity entity, MyComponent component) {
        // 添加延迟命令
        addDelayCommand(new EcsCommandAddComponent(entity, new NewComponent()), 
                      EcsCommandScope.SYSTEM);
    }
}
```

### 可用的延迟命令

GServerECS提供了以下四种延迟命令：

- **EcsCommandCreateEntity**: 延迟创建实体
- **EcsCommandDestroyEntity**: 延迟销毁实体
- **EcsCommandAddComponent**: 延迟添加组件
- **EcsCommandRemoveComponent**: 延迟移除组件

### 命令作用域

延迟命令支持三种作用域，控制命令的执行时机：

- **`SYSTEM`**: 系统作用域，命令在当前System执行完成后执行
- **`SYSTEM_GROUP`**: 系统组作用域，命令在当前系统组执行完成后执行
- **`WORLD`**: 世界作用域，命令在本次世界update完成后执行


## 🎮 实体操作生效时机


GServerECS中的实体操作分为**立即生效**和**延迟生效**两种模式：

### 立即生效操作
- **实体添加**: 通过`ecsworld.createEntity()`调用
- **组件添加/移除**: 通过`entity.addComponent()`和`entity.removeComponent()`直接调用
- **生效时机**: 操作立即生效，当前System执行结束后即可被其他System访问

### 延迟生效操作

#### 实体销毁
- **操作方式**: 通过`world.requestDestroyEntity()`请求销毁
- **生效时机**: 在本次世界update完成后执行，确保所有System都能处理该实体

#### 延迟命令操作
- **所有操作**: 通过延迟命令系统执行（EcsCommandCreateEntity、EcsCommandDestroyEntity、EcsCommandAddComponent、EcsCommandRemoveComponent）
- **生效时机**: 参考章节[延迟命令系统](#-延迟命令系统)

## 🧪 测试示例

项目包含丰富的测试用例，展示了各种功能的使用方法：

- **组件操作测试**: 演示组件的添加、移除操作（立即和延迟）
- **实体操作测试**: 演示实体的创建、销毁操作（立即和延迟）
- **系统测试**: 演示系统执行顺序控制、更新间隔功能和复杂系统组合的使用
- **资源清理测试**: 演示ECS世界销毁和资源清理功能


## 📁 项目结构

```
src/
├── main/java/top/kgame/lib/ecs/
│   ├── annotation/          # 注解定义
│   ├── command/            # 命令系统
│   ├── core/               # 核心实现
│   ├── exception/          # 异常定义
│   ├── extensions/         # 扩展功能
│   └── tools/              # 工具类
└── test/java/top/kgame/lib/ecstest/
    ├── component/          # 组件测试
    │   ├── add/            # 组件添加测试
    │   └── remove/         # 组件移除测试
    ├── entity/             # 实体测试
    │   ├── add/            # 实体添加测试
    │   └── remove/         # 实体移除测试
    ├── system/             # 系统测试
    │   ├── interval/       # 系统间隔测试
    │   ├── mixed/          # 混合系统测试
    │   └── order/          # 系统顺序测试
    │       ├── custom/     # 自定义顺序测试
    │       └── def/        # 默认顺序测试
    └── dispose/            # 资源清理测试
```

## 📋 后续开发计划

- 多线程支持(进行中)
- 脱离System的Entity-Component框架(未开始)

## 📄 许可证

本项目采用 Apache License 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🔗 相关链接

- [项目主页](https://github.com/ZKGameDev/GServerECS)
- [问题反馈](https://github.com/ZKGameDev/GServerECS/issues)

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue: [GitHub Issues](https://github.com/ZKGameDev/GServerECS/issues)
- 邮箱: chinazhangk@gmail.com

---
