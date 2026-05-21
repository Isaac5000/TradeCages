# NeoForge Modding Blueprint

A production-grade, scalable architecture template for Minecraft NeoForge modding projects.

This blueprint provides a battle-tested structure for building maintainable, extensible mods with strong separation of concerns. It's designed to handle the complexity of modern Minecraft mod development while remaining approachable for new projects.

## 📋 Core Separations

- **Gameplay Logic** - Pure business rules independent of frameworks
- **Minecraft Internals** - Minecraft API abstractions
- **NeoForge APIs** - Framework-specific implementation
- **Rendering Systems** - Visual output through abstracted interfaces
- **Networking** - Packet handling and synchronization
- **Persistence** - Data storage (NBT, databases, files)
- **Client/Server Code** - Environment-specific implementations

---

# Architecture Overview

This project combines multiple proven architectural patterns to create a robust foundation for mod development.

## Feature-Based Organization

Instead of organizing by technical layers (controllers, services, repositories), this template organizes by **gameplay features**.

```text
feature/
    mana/                  (Magic system)
    quest/                 (Quest system)
    machine/               (Automation system)
    inventory/             (Custom storage)
```

Each feature is **fully self-contained** and owns:

- Domain entities and business logic
- Use cases (application layer)
- Data persistence
- Networking and synchronization
- Client rendering and UI
- Infrastructure adapters

**Benefits:**

- ✅ Easier to onboard new developers ("work on the mana feature")
- ✅ Simple to remove features (delete the folder)
- ✅ Clear ownership boundaries
- ✅ Reduced merge conflicts
- ✅ Natural code organization

---

## Hexagonal Architecture (Ports & Adapters)

Your game logic communicates through **interfaces (ports)**, not direct implementations.

```text
Input (NeoForge Event)
    ↓
Input Port (Interface)
    ↓
Use Case (Pure logic)
    ↓
Output Port (Interface)
    ↓
Output Adapter (Minecraft/NBT/Network)
```

**Why?**

- Framework changes only affect adapters
- Logic remains testable in isolation
- Easy to swap implementations
- Reduces coupling to Minecraft versions

---

## Clean Architecture Dependency Flow

All dependencies point toward the core:

```text
NeoForge APIs / Minecraft
        ↓
Adapters Layer
        ↓
Application Layer (Use Cases)
        ↓
Domain Layer (Pure Logic)
```

The domain layer must **never** import anything from Minecraft or NeoForge.

---

## Anti-Corruption Layer (ACL)

Minecraft-specific types are immediately translated to internal abstractions:

**Instead of:**
```java
public void handlePlayer(ServerPlayer player) {
    // Now you have a Minecraft-specific class
}
```

**Use:**
```java
public void handlePlayer(GamePlayer player) {
    // Internal abstraction, independent of Minecraft versions
}
```

This dramatically reduces the impact of Mojang refactors.

---

# Project Structure

```text
src/main/java/com/example/examplemod/

├── feature/
│   └── featureexample/          (Example feature - duplicate to create new ones)
│       ├── adapters/
│       │   ├── input/           (Event handlers, NeoForge listeners)
│       │   └── output/          (Persistence, rendering, networking)
│       ├── application/
│       │   ├── port/
│       │   │   ├── input/       (Input interfaces)
│       │   │   └── output/      (Output interfaces)
│       │   └── usecase/         (Application logic using ports)
│       │
│       └── domain/
│           └── model/           (Entities, value objects, domain events)
│
├── platform/
│   └── neoforge/                (NeoForge integration layer)
│       ├── bootstrap/           (Mod initialization)
│       │   ├── ExampleMod.java                 (Server + common setup)
│       │   ├── ExampleModClient.java           (Client setup)
│       │   └── Config.java                     (Configuration)
│       ├── event/               (NeoForge event handlers)
│       └── registration/        (Registering blocks, items, etc.)
│
└── shared/                      (Optional: shared abstractions)
    ├── application/
    ├── domain/
    └── adapters/
```

---

# Core Principles

## 1️⃣ Domain Never Touches Minecraft

**These imports belong ONLY in adapters/platform:**

```java
net.minecraft.*
net.neoforged.*
```

**✅ Allowed in domain/:**
```java
java.util.*
java.lang.*
com.example.examplemod.feature.*/domain/*
com.example.examplemod.shared/domain/*
```

---

## 2️⃣ Dependencies Flow Inward

```text
minecraft-implementation (NeoForge events, etc.)
        ↓
adapters-layer (Actually implements the ports)
        ↓
application-usecases (Orchestrates domain logic)
        ↓
domain-logic (Pure business rules)
```

**Never the opposite.**

---

## 3️⃣ Features Own Their Stack

Each feature is responsible for:

- ✅ Its own use cases
- ✅ Its own domain models
- ✅ Its own persistence
- ✅ Its own networking
- ✅ Its own rendering
- ✅ Its own configuration

This makes features **easily testable** and **independently deployable**.

---

## 4️⃣ Minecraft Is a Platform

Treat Minecraft like a library you depend on, not the core of your app.

**Adapters** translate between your world and Minecraft's world:

```text
com.example.examplemod.platform.minecraft.adapter/
    - PlayerAdapter          (ServerPlayer → GamePlayer)
    - WorldAdapter          (ServerLevel → GameWorld)
    - InventoryAdapter      (Container → GameInventory)
    - RendererAdapter       (Renderer → GameHudRenderer)
```

When Minecraft changes, only these adapters need updates.

---

# Creating Your First Feature

## Step 1: Create the Feature Directory

```bash
src/main/java/com/example/examplemod/feature/YOUR_FEATURE_NAME/
```

Example:
```bash
src/main/java/com/example/examplemod/feature/mana/
```

## Step 2: Create the Standard Structure

```text
mana/
├── application/
│   ├── port/
│   │   ├── input/
│   │   │   └── ManaInputPort.java
│   │   └── output/
│   │       └── ManaOutputPort.java
│   └── usecase/
│       ├── IncreaseManaUseCase.java
│       └── DecreaseManaUseCase.java
│
├── domain/
│   └── model/
│       ├── Mana.java                  (Value object)
│       ├── ManaPool.java              (Entity)
│       └── ManaChangedEvent.java      (Domain event)
│
└── adapters/
    ├── input/
    │   └── ManaEventListener.java  (NeoForge @Mod.EventBusSubscriber)
    └── output/
        ├── ManaRepository.java
        ├── ManaRenderer.java
        ├── network/
        │   └── ManaUpdatePacket.java
        └── persistence/
            └── ManaNBTHandler.java
```

## Step 3: Example Implementation

**domain/model/Mana.java** (Pure domain logic):
```java
public record Mana(int current, int maximum) {
    public Mana {
        if (current < 0 || current > maximum || maximum < 0) {
            throw new IllegalArgumentException("Invalid mana values");
        }
    }
    
    public Mana regenerate(int amount) {
        return new Mana(Math.min(current + amount, maximum), maximum);
    }
    
    public Mana consume(int amount) {
        if (current < amount) {
            throw new IllegalStateException("Not enough mana");
        }
        return new Mana(current - amount, maximum);
    }
}
```

**application/port/output/ManaOutputPort.java**:
```java
public interface ManaOutputPort {
    void saveMana(UUID playerId, Mana mana);
    void notifyManaChanged(UUID playerId, Mana mana);
}
```

**application/usecase/IncreaseManaUseCase.java**:
```java
public class IncreaseManaUseCase {
    private final ManaRepository repository;
    private final ManaOutputPort output;
    
    public void execute(UUID playerId, int amount) {
        Mana current = repository.getMana(playerId);
        Mana updated = current.regenerate(amount);
        
        output.saveMana(playerId, updated);
        output.notifyManaChanged(playerId, updated);
    }
```

**adapters/input/ManaEventListener.java**:
```java
@Mod.EventBusSubscriber(modid = ExampleMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManaEventListener {
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        var player = event.getEntity();
        if (player.level().isClientSide) return;
        
        // Call your use case
        increaseManaUseCase.execute(player.getUUID(), REGEN_AMOUNT);
    }
}
```

---

# Getting Started

## Prerequisites

- Java 25+
- NeoForge 26.1.2+ (or your target version)
- Gradle 8.0+
- Git

## Quick Setup

### 1. Clone This Repository

```bash
git clone <this-repo>
cd NeoForge-Blueprint
```

### 2. Update gradle.properties

Edit `gradle.properties`:

```properties
# Set your Minecraft version
minecraft_version=26.1.2
neo_version=26.1.2.43-beta

# Your mod identifier
mod_id=mymod
mod_name=My Mod
mod_version=1.0.0

# Your Java package
group_id=com.yourname.mymod
```

### 3. Update neoforge.mods.toml

Edit `src/main/templates/META-INF/neoforge.mods.toml`:

```toml
displayName="My Mod Name"
description="Your mod description"
authors="Your Name"
displayURL="https://yourwebsite.com"
logoFile="logo.png"
```

### 4. Rename Your Package

Rename the Java package from `com.example.examplemod` to `com.yourname.yourmod`:

1. In IDEA: Refactor → Rename Package
2. Or manually move files to the correct directories

### 5. Run the Development Environment

**Client:**
```bash
./gradlew runClient
```

**Server:**
```bash
./gradlew runServer
```

**Data Generation:**
```bash
./gradlew runData
```

---

# Configuration Guide

## gradle.properties

| Property | Example | Purpose |
|----------|---------|---------|
| `minecraft_version` | `26.1.2` | Target Minecraft version |
| `neo_version` | `26.1.2.43-beta` | NeoForge loader version |
| `mod_id` | `mymod` | Unique mod identifier (lowercase, no spaces) |
| `mod_name` | `My Awesome Mod` | Human-readable mod name |
| `mod_version` | `1.0.0` | Current version (semantic versioning) |
| `mod_license` | `MIT` | License (MIT, GPL-3.0, Apache-2.0, etc.) |
| `group_id` | `com.yourname.mymod` | Java package namespace |
| `minecraft_version_range` | `[26.1.2]` | Compatible Minecraft versions |

## build.gradle

### Java Version

```gradle
java.toolchain.languageVersion = JavaLanguageVersion.of(25)
```

Match the Java version with your Minecraft version:
- Minecraft 26.1+ → Java 25
- Minecraft 25.x → Java 23-24

### Adding Dependencies

```gradle
dependencies {
    // Example: JEI integration
    compileOnly "mezz.jei:jei-${minecraft_version}-common-api:${jei_version}"
    
    // Example: External library
    implementation "org.apache.commons:commons-lang3:3.12.0"
}
```

Find versions at [Maven Central](https://mvnrepository.com/)

---

# Best Practices

## ✅ Do This

| Practice | Example |
|----------|---------|
| **Isolate Minecraft code** | Use adapters for all Minecraft imports |
| **Keep domain pure** | No framework imports in `domain/` |
| **Encapsulate features** | Each feature owns its domain logic |
| **Use ports/adapters** | Interfaces between layers |
| **Abstract volatile APIs** | Minecraft changes frequently |
| **Write testable code** | Domain logic should be unit-testable |
| **Document architecture decisions** | Why, not just what |
| **Use meaningful names** | Classes should describe their purpose |

## ❌ Avoid This

| Anti-Pattern | Why |
|--------------|-----|
| **Static managers** | Hard to test, difficult to reason about |
| **Giant event handlers** | Becomes a god class |
| **Logic in renderers** | Breaks separation of concerns |
| **Minecraft imports in domain** | Creates tight coupling |
| **God classes** | One class doing everything |
| **Circular dependencies** | Causes maintainability issues |
| **Magic numbers/strings** | Use constants with clear names |
| **Deep nesting** | Hard to follow logic flow |

---

# Layering Guidelines

## 🎮 Domain Layer (`domain/`)

**Responsibility:** Pure business logic

**Contains:**
- Entities (objects with identity)
- Value Objects (immutable data)
- Domain Services (business operations)
- Domain Events (state changes)
- Exceptions (domain-specific errors)

**Never imports:**
```java
net.minecraft.*
net.neoforged.*
```

**Example:**
```java
public class ManaPool {
    private int current;
    private int maximum;
    
    public void regenerate(int amount) {
        current = Math.min(current + amount, maximum);
    }
}
```

---

## 🔌 Application Layer (`application/`)

**Responsibility:** Use cases and orchestration

**Input Ports:** Interfaces that external code calls (e.g., event handlers)

**Output Ports:** Interfaces that this layer calls (e.g., repositories, notifications)

**Use Cases:** Combine domain logic with ports to implement features

**Never imports:**
```java
net.minecraft.*
net.neoforged.*
```

**Example:**
```java
public interface ManaInputPort {
    void handlePlayerManaRegeneration(UUID player);
}

public class ManaUseCase implements ManaInputPort {
    private ManaRepository repo;
    private ManaNotifier notifier;
    
    public void handlePlayerManaRegeneration(UUID player) {
        var mana = repo.get(player);
        var updated = mana.regenerate(10);
        repo.save(player, updated);
        notifier.notify(player, updated);
    }
}
```

---

## 🔧 Adapters Layer (`adapters/`)

**Responsibility:** External implementation details

**Input Adapters:** Listen to NeoForge events, call use cases

**Output Adapters:** Implement persistence, rendering, networking

**Imports allowed:**
```java
net.minecraft.*
net.neoforged.*
// Feel free to use framework-specific code here
```

**Example:**
```java
@Mod.EventBusSubscriber(modid = MOD_ID)
public class ManaEventAdapter {
    static ManaUseCase useCase = new ManaUseCase(...);
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        useCase.handlePlayerManaRegeneration(event.getEntity().getUUID());
    }
}
```

---

## 🎨 Client Layer (Optional)

**Responsibility:** Client-only rendering and UI

**Contains:**
- HUD renderers
- Screen displays
- Client events
- Particle effects

**Important:** No gameplay logic here! Only rendering.

**Example:**
```java
public class ManaHudRenderer {
    public void render(GuiGraphics graphics, int screenWidth, int screenHeight) {
        // Render mana bar, etc.
    }
}
```

---

## 🌐 Platform Layer (`platform/neoforge/`)

**Responsibility:** NeoForge and Minecraft integration

**bootstrap/:** Mod initialization
```text
ExampleMod.java           - Main mod class with @Mod annotation
ExampleModClient.java     - Client setup
Config.java              - Configuration management
```

**event/:** NeoForge event handlers

**registration/:** Block, item, entity registration

---

# Networking

## Packet Structure

Keep packets inside `adapters/output/network/`:

```text
adapters/output/network/
    ManaUpdatePacket.java          (Packet definition)
    ManaUpdatePacketHandler.java   (Packet logic)
```

**Example Packet:**
```java
public record ManaUpdatePacket(UUID playerId, int current, int maximum) 
    implements CustomPacketPayload {
    
    public static final ResourceLocation ID = 
        new ResourceLocation(ExampleMod.MOD_ID, "mana_update");
    
    public ManaUpdatePacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readInt(), buf.readInt());
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeInt(current);
        buf.writeInt(maximum);
    }
    
    @Override
    public ResourceLocation id() { return ID; }
}
```

---

# Persistence

## NBT Data

Store data using `CompoundTag`:

```java
public class ManaNBTHandler {
    public static CompoundTag save(Mana mana) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("current", mana.current());
        tag.putInt("maximum", mana.maximum());
        return tag;
    }
    
    public static Mana load(CompoundTag tag) {
        return new Mana(
            tag.getInt("current"),
            tag.getInt("maximum")
        );
    }
}
```

## Database (Optional)

For complex mods, consider:
- SQLite (lightweight)
- PostgreSQL (multiplayer)
- MongoDB (document-based)

Place database code in `adapters/output/persistence/`.

---

# Rendering

Isolate rendering logic:

```text
adapters/output/
    ManaHudRenderer.java
    ManaParticleRenderer.java
    ManaBlockRenderer.java
```

Never put rendering logic in:
- Domain
- Application

These are purely visual concerns.

---

# Migration & Upgrades

## Minecraft Version Upgrade

When upgrading Minecraft versions:

1. Update `gradle.properties`:
   ```properties
   minecraft_version=27.0.0
   neo_version=27.0.0.1-beta
   ```

2. Run `./gradlew build` and fix compilation errors

3. Most errors will be in `platform/` and `adapters/`

4. Domain logic should require minimal changes

## API Refactoring

If NeoForge changes an API:

1. Locate which adapter uses it → `adapters/`
2. Update the adapter implementation
3. If the port interface needs changing, update it
4. Domain layer is unaffected

---

# Project Scalability

This architecture scales from:

| Project Size | Examples |
|-------------|----------|
| **Small** (1-2 features) | EnderIO, Thermal, Quark |
| **Medium** (5-10 features) | Botania, Create, Immersive |
| **Large** (20+ features) | Modpack-like complexity, complex automation |
| **Multiplayer** | MMO-style mods, persistent worlds |

For single-feature mods, this structure may be overkill. Consider simplifying.

---

# Philosophy

> Minecraft is not the application core.
>
> Minecraft is the platform your application runs on.

Your mod is the real application. Minecraft is just the runtime environment.

This inversion of thinking fundamentally changes how you architect code:

- Domain logic survives version changes
- Features are testable independently
- New developers understand boundaries
- Refactoring is safer and easier

---

# Resources

## Official Documentation

- [NeoForged Docs](https://docs.neoforged.net/)
- [Minecraft Wiki](https://minecraft.wiki/)

## Community

- [NeoForged Discord](https://discord.neoforged.net/)
- [Minecraft Modding Wiki](https://minecraft.fandom.com/)

## Architecture References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design by Eric Evans](https://www.domainlanguage.com/ddd/)

---

# License

This blueprint is provided as-is. Modify freely for your projects.

## Next Steps

1. ✅ Read this entire document
2. ✅ Study the `ExampleMod` feature implementation
3. ✅ Create your first feature (delete `ExampleMod` first)
4. ✅ Build something awesome!

Happy modding! 🚀

