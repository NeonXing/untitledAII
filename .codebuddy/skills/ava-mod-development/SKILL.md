---
name: ava-mod-development
description: This skill provides specialized guidance for developing Minecraft AVA gun mods on Forge, including performance optimization, rendering techniques, code quality standards, and best practices learned from top mods like TaCZ, Embeddium, Lithium, and Starlight.
---

# AVA Gun Mod Development Skill

## Purpose

This skill provides comprehensive guidance for developing high-performance, visually impressive Minecraft gun mods (similar to A.V.A mod) on the Forge modloader. It combines best practices from top optimization and rendering mods with specific gun mod development patterns.

## When to Use This Skill

Use this skill when:
- Developing gun mechanics and firing systems
- Implementing gun rendering (first-person, third-person, GUI)
- Optimizing mod performance (FPS, TPS, memory)
- Working with particles, tracers, and visual effects
- Implementing attachment systems (scopes, muzzles, magazines)
- Managing gun state and data (ammo, reload, fire modes)

## Core Development Principles

### 1. Performance First

Always prioritize performance. Gun mods can significantly impact FPS due to:
- Many particle effects (muzzle flash, tracers, bullet hits)
- Complex 3D models in hand
- Real-time visual effects
- Frequent state updates during firing

Follow these rules:
- Use `tickCount % N == 0` to throttle logic execution
- Avoid object allocation in hot paths (render/tick loops)
- Cache frequently accessed data in `static final` fields
- Use object pools for particles and entities
- Implement distance culling for visual effects

### 2. Efficient Rendering

Gun rendering requires careful optimization:
- Use `IClientItemExtensions` for hand/GUI models (modern approach)
- Cache `BakedModel` instances
- Batch similar draw calls using `MultiBufferSource` and `VertexConsumer`
- Reuse `PoseStack` and buffers outside render loops
- Use appropriate `RenderType` for geometry
- Simplify first-person arm models

### 3. Optimized Visual Effects

#### Muzzle Flash
- Use `ParticleEngine` or custom `ParticleProvider`
- Never use full entities for simple particles
- Implement distance culling
- Limit particle count and use pooling

#### Tracers (Bullet Trails)
- Use `LineRenderer` or instanced quads
- Avoid per-frame `Vec3` allocations
- Reuse vertex buffers

#### Bullet Hits
- Use particle systems, not full entities
- Cache hit points
- Limit simultaneous hit effects

### 4. Algorithm Optimization

#### Pathfinding (if needed)
- Use optimized A* algorithm
- Choose appropriate heuristic (Octile for 3D, Manhattan for grid)
- Use binary heap or Fibonacci heap for open list
- Use HashSet/HashMap for closed list O(1) lookups
- Compress coordinates to long type
- Use hierarchical A* for long distances
- Implement path smoothing

#### Spatial Queries
- Cache chunk or height map data
- Use spatial indexing (octree) for fast queries
- Avoid repeated world queries in tick loops

### 5. Multi-threading

For CPU-intensive operations:
- Use asynchronous threads for pathfinding, world generation
- Use `CompletableFuture` or custom thread pools
- Never call `CompletableFuture.get()` on main thread
- Ensure thread-safe data access

## Code Quality Standards

### Naming Conventions
- Classes: `PascalCase` (e.g., `GunRegistry`, `BulletEntity`)
- Methods: `camelCase` (e.g., `fireBullet()`, `reload()`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_AMMO`, `FIRE_RATE`)
- Private fields: `camelCase` (e.g., `currentAmmo`, `isReloading`)

### Code Structure
- Single responsibility principle
- Methods under 200 lines
- Use design patterns (Factory, Singleton, Strategy)
- Avoid deep nesting (max 4 levels)

### Error Handling
- Add null checks for critical operations
- Use try-catch for resource loading
- Log exceptions with `LOGGER.error()`
- Never use empty catch blocks

### Logging
- Use `LogUtils.getLogger()`
- Levels: debug/trace (development), info (normal), warn (warnings), error (errors)
- Log critical operations with info
- Log performance issues with debug

### Comments
- Add JavaDoc for public APIs
- Add inline comments for complex logic
- Use TODO/FIXME for pending work
- Never comment out code - delete it instead

## Development Workflow

### 1. Initial Setup
- Follow Forge mod structure (`src/main/java`, `src/main/resources`)
- Use DeferredRegister for blocks, items, entities
- Set up capabilities for player data
- Configure event buses properly

### 2. Implement Core Systems
- Gun registry and data structure
- Firing mechanism (cooldown, damage, spread)
- Reload system (magazine, ammo types)
- Attachment system (scopes, muzzles, stocks)

### 3. Implement Rendering
- Create gun models (OBJ, JSON, or code-generated)
- Implement `IClientItemExtensions`
- Add first-person and third-person rendering
- Add GUI preview rendering

### 4. Add Visual Effects
- Muzzle flash particles
- Tracer rendering
- Bullet hit effects
- Smoke and impact particles

### 5. Performance Optimization
- Profile with Spark Profiler
- Identify bottlenecks (CPU vs GPU)
- Apply optimization techniques
- Test with and without optimization mods

### 6. Testing
- Test firing, reloading, attachments
- Test performance impact
- Test multi-player compatibility
- Test with different Minecraft versions

## Reference Mods and Projects

Study these projects for implementation patterns:
- **TaCZ (Timeless and Classics Zero)**: GitHub: `F1zeiL/TACZ` - Modern gun mod architecture
- **Embeddium**: GitHub: `ThatMG393/embeddium` - Rendering optimization techniques
- **Lithium**: GitHub: `CaffeineMC/lithium` - Game logic optimization
- **Starlight**: GitHub: `Spottedleaf/Starlight` - Lighting engine rewrite
- **C2ME**: GitHub: `RelativityMC/C2ME-fabric` - Multi-threading patterns
- **Iris**: GitHub: `IrisShaders/Iris` - Shader integration

## Performance Profiling Tools

- **Spark Profiler**: Detailed CPU/GPU breakdown
- **F3 Debug Screen**: Monitor ms/tick and FPS
- **Forge Debug Tools**: `/forge tps`, `/forge entity`
- **External Profilers**: RenderDoc, NVIDIA Nsight

## Common Pitfalls to Avoid

1. **Per-frame object allocations** in render methods
2. **Creating particles as entities** instead of using ParticleEngine
3. **Forgetting to cancel event listeners** after use
4. **Blocking main thread** with heavy computations
5. **Using reflection in hot paths**
6. **Loading resources every frame** instead of caching
7. **Ignoring distance culling** for visual effects
8. **Mixing synchronous and asynchronous** code incorrectly

## Optimization Checklist

Before releasing any feature:
- [ ] No object allocation in hot loops
- [ ] Event listeners properly cancelled when done
- [ ] Distance culling implemented for effects
- [ ] Models cached and reused
- [ ] Draw calls batched where possible
- [ ] Thread safety ensured for multi-threaded code
- [ ] Null checks for critical operations
- [ ] Appropriate logging levels used
- [ ] Spark profiler shows no significant bottlenecks

## Git Commit Conventions

Use conventional commits:
- `feat: add gun firing system`
- `fix: resolve crash on reload`
- `perf: optimize tracer rendering`
- `refactor: simplify gun registry`
- `docs: update README`

## Integration with Performance Mods

Design your mod to work well with:
- **ModernFix**: General performance improvements
- **Embeddium/Rubidium**: Client-side rendering
- **Lithium/Canary**: Game logic optimization
- **C2ME**: Multi-threaded chunk management
- **Starlight**: Lighting engine optimization
- **Dynamic FPS**: Background FPS reduction

Test with these mods to ensure compatibility.
