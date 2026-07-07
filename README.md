# GServerECS - Entity Component System Framework for Java Game Server

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/top.kgame/kgame-lib-ecs)](https://central.sonatype.com/artifact/top.kgame/kgame-lib-ecs)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)

<div align="center">

[English](README.md) | [中文](README_CN.md)

</div>

GServerECS is an open-source ECS framework designed and developed specifically for game servers, implemented in Java. This framework provides complete ECS architecture support, including runtime component addition/removal, system execution order control, on-the-fly and deferred loading of entities/components, and other key features.

This framework is designed for game server scenarios. A single process can create multiple EcsWorld instances, each corresponding to a game room (Room) or scene (Scene). Each EcsWorld is designed to be **non-thread-safe** and should only be used within a single thread. Cross-thread calls are not supported.

If this project has helped you, please feel free to give it a star⭐ to show your support~ This will help more people discover it 😊
## 🌟 Key Features

### Core Functionality
- **Entity Management**: Entity creation, destruction, and lifecycle management
- **Component System**: Support for dynamic component addition/removal with type safety
- **System Execution**: Flexible system update mechanism with multiple execution modes
- **Entity Prototypes**: Component-based entity prototype system

### Advanced Features
- **System Groups**: Support for system group management, facilitating complex logic organization
- **Execution Order Control**: Precise control of system execution order through annotations
- **Deferred Commands**: Support for deferred execution of entity operation commands
- **Entity Factories**: Factory pattern for entity creation, simplifying entity instantiation
- **Auto-scanning**: Automatic discovery and registration of systems, components, and factories based on package scanning

## 📋 System Requirements

- **Java**: Version 21 or higher
- **Maven**: Version 3.6 or higher
- **Dependencies**: 
  - Log4j2 (2.25.3)
  - Disruptor (3.4.4)
  - JUnit 5 (for testing)

## 🚀 Quick Start

### 1. Add Dependency

See the Maven Central badge above for the latest version, or visit [Maven Central](https://central.sonatype.com/artifact/top.kgame/kgame-lib-ecs) to copy the dependency snippet.

```xml
<dependency>
    <groupId>top.kgame</groupId>
    <artifactId>kgame-lib-ecs</artifactId>
    <version><!-- use the version shown on Maven Central --></version>
</dependency>
```

### 2. Create Components

```java
public class PositionComponent implements EcsComponent {
    public float x, y, z;
}

public class HealthComponent implements EcsComponent {
    public int currentHealth;
    public int maxHealth;
}
```

### 3. Create Systems

```java
@SystemGroup(GameSystemGroup.class) 
// Systems without @SystemGroup annotation belong to top-level systems, 
// at the same level as SystemGroups, all scheduled directly by EcsWorld
public class MovementSystem extends EcsOneComponentUpdateSystem<PositionComponent> {
    
    @Override
    protected void update(Entity entity, PositionComponent position) {
        // Update position logic
        position.x += 1.0f;
    }
}
```

### 4. Create Entity Factory

```java
// EntityFactory implementations are automatically scanned and registered
public class PlayerFactory extends BaseEntityFactory {

    @Override
    protected Collection<EcsComponent> generateComponent() {
      return List.of(new PositionComponent(), new HealthComponent());
    }
    
    @Override
    public int typeId() {
        return 1; // Factory type ID, must be unique within the same EcsWorld
    }
}
```

### 5. Create System Group

```java
public class GameSystemGroup extends EcsSystemGroup {
    // System group implementation
    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {
  
    }
}
```

### 6. Use ECS World

```java
public class Game {
    private EcsWorld world;
    
    public void init() {
        // Create ECS world, specify package names to scan
        world = EcsWorld.generateInstance("com.example.game");
        // Can set custom context
        world.setContext(this);
    }
    
    public void update(long currentTime) {
        // Update ECS world
        // Note: Timestamp must be strictly increasing, must be greater than the last passed time
        world.update(currentTime);
    }
    
    public void createPlayer() {
        // Create player entity through factory
        Entity player = world.createEntity(PlayerFactory.class);
    }
    
    public void cleanup() {
        world.close();
    }
}
```

### 7. Entity Operations

```java
// Get component
PositionComponent position = entity.getComponent(PositionComponent.class);

// Check component
if (entity.hasComponent(HealthComponent.class)) {
    // Handle logic
}

// Add component
entity.addComponent(new HealthComponent());

// Remove component
entity.removeComponent(PositionComponent.class);

// Destroy entity
world.requestDestroyEntity(entity);
```

## 📖 Annotations

GServerECS provides rich annotations to control system behavior:

### System Control Annotations

#### @SystemGroup
- **Purpose**: Marks an EcsSystem to execute updates within a specified EcsSystemGroup
- **Target**: EcsSystem classes
- **Parameters**: `Class<? extends EcsSystemGroup> value()` - System group type
- **Description**: EcsSystem marked with this annotation will execute updates within the specified EcsSystemGroup. EcsSystem not marked with this annotation belong to top-level systems at the same level as EcsSystemGroup, scheduled by EcsWorld. **Note: This annotation cannot be used on EcsSystemGroup classes, and nested SystemGroups are not currently supported.**

#### @TickRate
- **Purpose**: Marks system update interval time
- **Target**: EcsSystem classes
- **Parameters**: `int value()` - Update interval time (milliseconds)
- **Description**: Systems marked with this annotation will execute updates after the specified time interval. Systems not marked with this annotation will execute every update cycle.

#### @Standalone
- **Purpose**: Marks EcsSystem to always execute updates, regardless of whether there are matching entities
- **Target**: EcsSystem classes
- **Parameters**: None
- **Description**: EcsSystem marked with this annotation will execute in every update cycle, even if no entities contain the components required by this EcsSystem. EcsSystem not marked with this annotation will only execute updates in each update cycle when there are entities containing the required components.

#### @After
- **Purpose**: Marks EcsSystem to execute updates after specified EcsSystem within the same group
- **Target**: EcsSystem classes
- **Parameters**: `Class<? extends EcsSystem>[] value()` - Target system type array
- **Description**: EcsSystem marked with this annotation will execute updates after the specified EcsSystem completes. EcsSystem with the same conditions will execute in dictionary order. Can be used in SystemGroup.

#### @Before
- **Purpose**: Marks EcsSystem to execute updates before specified EcsSystem within the same group
- **Target**: EcsSystem classes
- **Parameters**: `Class<? extends EcsSystem>[] value()` - Target system type array
- **Description**: EcsSystem marked with this annotation will execute updates before the specified EcsSystem. EcsSystem with the same conditions will execute in dictionary order. Can be used in SystemGroup.

### Entity Factory

EntityFactory implementations are automatically scanned and registered by EcsWorld. No annotation is required. Simply implement the EntityFactory interface or extend BaseEntityFactory, and the framework will automatically discover and register your factory classes.

## 🔧 Predefined System Types

GServerECS provides various predefined system base classes:

- `EcsOneComponentUpdateSystem<T>`: System handling entities with a single specified component
- `EcsTwoComponentUpdateSystem<T1, T2>`: System handling entities with two specified components
- `EcsThreeComponentUpdateSystem<T1, T2, T3>`: System handling entities with three specified components
- `EcsFourComponentUpdateSystem<T1, T2, T3, T4>`: System handling entities with four specified components
- `EcsFiveComponentUpdateSystem<T1, T2, T3, T4, T5>`: System handling entities with five specified components
- `EcsStandaloneUpdateSystem`: Singleton update system, not bound to entities, executes once per world update
- `EcsExcludeComponentUpdateSystem<T>`: System handling entities that do not contain the specified component T
- `EcsInitializeSystem<T>`: Entity initialization system, automatically adds initialization completion marker
- `EcsDestroySystem<T>`: Entity destruction system, handles entities marked for destruction
- `EcsLogicSystem`: Logic system base class, provides component filtering and entity query functionality

## 📦 System Groups (EcsSystemGroup)

System Groups (EcsSystemGroup) are an important mechanism in GServerECS for organizing and managing system execution. A system group is itself a system that can contain multiple subsystems and execute them in a specific order.

### System Group Features

- **Automatic Management**: System groups automatically scan and manage all systems marked with the `@SystemGroup` annotation
- **Execution Order**: Systems within a system group execute according to the order defined by `@After` and `@Before` annotations
- **Lifecycle**: System groups have complete lifecycle management, including initialization, updates, and destruction
- **Dynamic Management**: Support for adding and removing Systems at runtime

### System Group Hierarchy

```
EcsWorld
├── Top-level Systems (without @SystemGroup annotation)
│   ├── SystemA
│   └── SystemB
└── System Groups
    ├── GameSystemGroup
    │   ├── InputSystem
    │   ├── LogicSystem
    │   └── RenderSystem
    └── PhysicsSystemGroup
        ├── CollisionSystem
        └── MovementSystem
```

## ⚡ Deferred Command System

GServerECS provides a complete deferred command system that allows safe execution of entity and component operations during system execution. Deferred commands execute within specified scopes, ensuring atomicity and consistency of operations.

```java
public class MySystem extends EcsOneComponentUpdateSystem<MyComponent> {
    
    @Override
    protected void update(Entity entity, MyComponent component) {
        // Add deferred command
        addDelayCommand(new EcsCommandAddComponent(entity, new NewComponent()), 
                      EcsCommandScope.SYSTEM);
    }
}
```

### Available Deferred Commands

GServerECS provides the following four deferred commands:

- **EcsCommandCreateEntity**: Deferred entity creation
- **EcsCommandDestroyEntity**: Deferred entity destruction
- **EcsCommandAddComponent**: Deferred component addition
- **EcsCommandRemoveComponent**: Deferred component removal

### Command Scopes

Deferred commands support three scopes that control command execution timing:

- **`SYSTEM`**: System scope, commands execute after the current System completes
- **`SYSTEM_GROUP`**: System group scope, commands execute after the current system group completes
- **`WORLD`**: World scope, commands execute after the current world update completes

## 🎮 Entity Operation Timing

GServerECS divides entity operations into **immediate effect** and **deferred effect** modes:

### Immediate Effect Operations
- **Entity Addition**: Through `ecsworld.createEntity()` calls
- **Component Addition/Removal**: Through direct calls to `entity.addComponent()` and `entity.removeComponent()`
- **Effect Timing**: Operations take effect immediately, accessible by other Systems after the current System completes

### Deferred Effect Operations

#### Entity Destruction
- **Operation Method**: Request destruction through `world.requestDestroyEntity()`
- **Effect Timing**: Executes after the current world update completes, ensuring all Systems can process the entity

#### Deferred Command Operations
- **All Operations**: Execute through the deferred command system (EcsCommandCreateEntity, EcsCommandDestroyEntity, EcsCommandAddComponent, EcsCommandRemoveComponent)
- **Effect Timing**: Refer to the [Deferred Command System](#-deferred-command-system) section

## 🧪 Test Examples

The project includes comprehensive test cases demonstrating various functionality usage:

- **Component Operation Tests**: Demonstrates component addition and removal operations (immediate and deferred)
- **Entity Operation Tests**: Demonstrates entity creation and destruction operations (immediate and deferred)
- **System Tests**: Demonstrates system execution order control, update interval functionality, and complex system combination usage
- **Resource Cleanup Tests**: Demonstrates ECS world destruction and resource cleanup functionality

## 📁 Project Structure

```
src/
├── main/java/top/kgame/lib/ecs/
│   ├── annotation/          # Annotation definitions
│   │   ├── After.java       # System execution order control (after)
│   │   ├── Before.java      # System execution order control (before)
│   │   ├── Standalone.java  # Standalone system marker
│   │   ├── SystemGroup.java # System group marker
│   │   └── TickRate.java    # System update interval
│   ├── command/            # Deferred command system
│   │   ├── EcsCommand.java # Command interface
│   │   ├── EcsCommandBuffer.java # Command buffer
│   │   ├── EcsCommandScope.java  # Command scope
│   │   ├── EcsCommandAddComponent.java
│   │   ├── EcsCommandCreateEntity.java
│   │   ├── EcsCommandDestroyEntity.java
│   │   └── EcsCommandRemoveComponent.java
│   ├── core/               # Core implementation
│   │   ├── ComponentFilter.java      # Component filter
│   │   ├── ComponentFilterMode.java # Filter mode
│   │   ├── ComponentFilterParam.java # Filter parameter
│   │   ├── EcsComponentManager.java  # Component manager
│   │   ├── EcsEntityManager.java     # Entity manager
│   │   ├── EcsSystemManager.java     # System manager
│   │   ├── EntityArchetype.java      # Entity archetype
│   │   ├── EntityFactory.java        # Entity factory interface
│   │   ├── EntityQuery.java          # Entity query
│   │   └── SystemScheduler.java      # System scheduler
│   ├── exception/          # Exception definitions
│   ├── extensions/         # Extension functionality
│   │   ├── component/      # Extension components
│   │   ├── entity/         # Extension entity factories
│   │   └── system/         # Extension system base classes
│   ├── tools/              # Utility classes
│   ├── EcsComponent.java   # Component interface
│   ├── EcsEntity.java      # Entity class
│   ├── EcsSystem.java      # System base class
│   ├── EcsSystemGroup.java # System group base class
│   └── EcsWorld.java       # ECS world
└── test/java/top/kgame/lib/ecstest/
    ├── component/          # Component tests
    │   ├── add/            # Component addition tests
    │   │   ├── immediately/ # Immediate addition
    │   │   └── delay/       # Deferred addition
    │   └── remove/         # Component removal tests
    │       ├── immediately/ # Immediate removal
    │       └── delay/       # Deferred removal
    ├── entity/             # Entity tests
    │   ├── add/            # Entity addition tests
    │   └── remove/         # Entity removal tests
    ├── schedule/           # System scheduling tests
    ├── system/             # System tests
    ├── core/               # Core functionality tests
    ├── performance/        # Performance tests
    ├── dispose/            # Resource cleanup tests
    └── util/               # Test utility classes
```

## 📋 Subsequent Development Plan

- Multi-thread Support (In Progress)
- Entity-Component Framework Independent of System (Not Started)

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🔗 Related Links

- [Project Homepage](https://github.com/top-kgame/GServerECS)
- [Issue Reporting](https://github.com/top-kgame/GServerECS/issues)

## 📞 Contact

For questions or suggestions, please contact us through:

- Submit Issue: [GitHub Issues](https://github.com/top-kgame/GServerECS/issues)
- Email: chinazhangk@gmail.com

--- 
