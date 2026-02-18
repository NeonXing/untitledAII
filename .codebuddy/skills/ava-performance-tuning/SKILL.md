---
name: ava-performance-tuning
description: This skill should be used when analyzing, optimizing, or profiling performance issues in the AVA Minecraft mod. Covers tick optimization, rendering optimization, memory management, and profiling tools usage.
---

# AVA Performance Tuning Skill

## Purpose

Identify and resolve performance bottlenecks in the AVA mod using systematic analysis, profiling tools, and proven optimization techniques from top performance mods.

## When to Use

Use this skill when:
- The mod causes low FPS or high lag
- Tick times are too high (visible in F3 debug screen)
- Memory usage is excessive
- Rendering performance needs improvement
- Profiling needs to be performed

## Performance Analysis Workflow

### Step 1: Identify the Problem

1. **Gather metrics:**
   - Press F3 to see ms/tick and FPS
   - Note which dimension/biome has issues
   - Check if issue is client-side (FPS) or server-side (TPS)

2. **Profile with Spark Profiler:**
   - Install Spark mod: https://www.curseforge.com/minecraft/mc-mods/spark
   - Run `/spark profiler` for 60 seconds
   - Download profile: `/spark profiler stop`
   - Analyze the warm-up and slow-loading areas

### Step 2: Categorize the Bottleneck

- **Tick bottleneck** - High ms/tick, low TPS
- **Render bottleneck** - Low FPS, ms/tick is fine
- **Memory bottleneck** - High RAM usage, frequent GC
- **Network bottleneck** - Packet loss or high bandwidth

### Step 3: Apply Optimizations

See optimization sections below based on category.

## Tick Optimization

### Throttling Pattern

```java
// Execute every N ticks
if (player.tickCount % 10 == 0) {
    // Logic that runs every 0.5 seconds
}

// Use Phase.END to avoid duplicate calls
@SubscribeEvent
public void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
        // Process once per tick
    }
}
```

### Event Listener Cleanup

```java
// Unregister when no longer needed
MinecraftForge.EVENT_BUS.unregister(this);

// Use WeakReference to prevent memory leaks
private final WeakReference<Level> worldRef;
```

### Caching Frequently Accessed Data

```java
// Cache field lookups
private static final Field ENTITY_HEIGHT_FIELD = ...
static {
    ENTITY_HEIGHT_FIELD.setAccessible(true);
}

// Cache computed values
private int cachedDistance = -1;
private BlockPos cachedTarget = null;
```

## Rendering Optimization

### Batch Rendering

```java
// Use MultiBufferSource for batching
PoseStack poseStack = new PoseStack();
MultiBufferSource.BufferSource buffer = 
    MultiBufferSource.immediate(LeavesBlock.SHEAR);

// Draw multiple elements
for (GunModelPart part : parts) {
    part.render(poseStack, buffer, light, overlay);
}

// Flush buffers once
buffer.endBatch();
```

### Distance Culling

```java
private static final float MAX_RENDER_DISTANCE_SQ = 64 * 64;

public boolean shouldRender(GunEntity gun, float distanceSq) {
    return distanceSq < MAX_RENDER_DISTANCE_SQ;
}
```

### Vertex Buffer Reuse

```java
// Reuse PoseStack and buffers
private final PoseStack poseStack = new PoseStack();
private final List<VertexConsumer> vertexConsumers = new ArrayList<>();

// Avoid creating objects in render loops
// BAD: for (int i = 0; i < 100; i++) { new Vec3(x, y, z); }
// GOOD: Vec3 temp = new Vec3(); for (int i = 0; i < 100; i++) { temp.set(x, y, z); }
```

### Model Caching

```java
// Cache BakedModel instances
private static final Map<ResourceLocation, BakedModel> MODEL_CACHE = new HashMap<>();

public BakedModel getModel(ResourceLocation id) {
    return MODEL_CACHE.computeIfAbsent(id, loc -> 
        Minecraft.getInstance().getModelManager().getModel(loc)
    );
}
```

## Memory Optimization

### Object Pooling

```java
public class ParticlePool {
    private static final Queue<Particle> POOL = new ArrayDeque<>();
    
    public static Particle acquire() {
        return POOL.isEmpty() ? new Particle() : POOL.poll();
    }
    
    public static void release(Particle particle) {
        particle.reset();
        POOL.offer(particle);
    }
}
```

### Primitive Types Over Wrapper Classes

```java
// Use int, float, boolean instead of Integer, Float, Boolean
// Prefer primitive arrays over List<Integer>
private int[] damageValues = new int[100];
```

### Flyweight Pattern for Immutable Data

```java
// Share GunData instances for all guns of same type
private static final Map<String, GunData> GUN_DATA_CACHE = new HashMap<>();
```

## Algorithm Optimization

### A* Pathfinding

```java
// Use Fibonacci heap for open list
PriorityQueue<Node> openList = new PriorityQueue<>(
    Comparator.comparingInt(Node::getF)
);

// Use HashSet for closed list O(1) lookup
Set<Node> closedSet = new HashSet<>();

// Coordinate compression: pack (x, y, z) into long
private static long packCoords(int x, int y, int z) {
    return ((long)x & 0xFFFFFFL) << 40 |
           ((long)y & 0xFFFFFFL) << 20 |
           ((long)z & 0xFFFFFFL);
}
```

### Spatial Partitioning

```java
// Use octree for 3D space queries
public class Octree<T> {
    private static final int MAX_OBJECTS = 8;
    private static final int MAX_DEPTH = 6;
    
    public List<T> query(AABB bounds) {
        // Fast spatial query O(log n)
    }
}
```

## Multithreading

### Async Pathfinding

```java
public class AsyncPathfinder {
    private static final ExecutorService EXECUTOR = 
        Executors.newFixedThreadPool(2);
    
    public CompletableFuture<Path> findPath(BlockPos start, BlockPos end) {
        return CompletableFuture.supplyAsync(() -> {
            return aStarPathfind(start, end);
        }, EXECUTOR);
    }
}
```

**Important:** Never block the main thread with `CompletableFuture.get()`

## Profiling Tools

### Spark Profiler

```bash
/spark profiler start
# Wait 60 seconds
/spark profiler stop
# Download and analyze in Spark Viewer
```

### F3 Debug Screen

- **FPS** - Render performance
- **ms/tick** - Server tick time (should be <50ms)
- **C** - Chunk sections rendered
- **E** - Entities rendered

### VisualVM

```bash
visualvm
```
Attach to Minecraft process to analyze memory and CPU.

## Common Anti-Patterns to Avoid

1. **Creating objects in hot paths**
   ```java
   // BAD
   for (Entity e : level.getEntitiesOfClass(Entity.class, bounds)) {
       new Vec3(e.position());
   }
   
   // GOOD
   Vec3 temp = new Vec3();
   for (Entity e : level.getEntitiesOfClass(Entity.class, bounds)) {
       temp.set(e.position());
   }
   ```

2. **Reflection in tick/render loops**
   ```java
   // BAD
   Field f = MyClass.class.getDeclaredField("value");
   
   // GOOD (cache field)
   private static final Field VALUE_FIELD = ...;
   static { VALUE_FIELD.setAccessible(true); }
   ```

3. **Unnecessary event listeners**
   ```java
   // Clean up when not needed
   MinecraftForge.EVENT_BUS.unregister(handler);
   ```

4. **String concatenation in loops**
   ```java
   // BAD
   String s = "";
   for (Item i : items) s += i.getName();
   
   // GOOD
   StringBuilder sb = new StringBuilder();
   for (Item i : items) sb.append(i.getName());
   ```

## Performance Checklist

Before marking optimization complete:

- [ ] Profiled with Spark to identify actual bottleneck
- [ ] Applied appropriate optimization technique
- [ ] Re-profiled to verify improvement
- [ ] No performance regression in other areas
- [ ] Code remains readable and maintainable
- [ ] Added comments explaining optimization rationale

## References

- `.codebuddy/rules/ava-performance-optimization` - Performance rules
- `.codebuddy/references/optimization-techniques.md` - Detailed techniques
- Spark Profiler: https://github.com/lucko/spark
- Lithium: https://github.com/CaffeineMC/lithium-fabric
- Embeddium: https://github.com/Embeddium/Embeddium
