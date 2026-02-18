# Optimization Techniques Reference

## Memory Optimization

### 1. Object Pooling
Use object pools for frequently created/destroyed objects:

```java
public class ObjectPool<T> {
    private final Queue<T> pool;
    private final Supplier<T> factory;
    
    public ObjectPool(Supplier<T> factory, int initialSize) {
        this.factory = factory;
        this.pool = new ArrayDeque<>(initialSize);
        for (int i = 0; i < initialSize; i++) {
            pool.offer(factory.get());
        }
    }
    
    public T obtain() {
        return pool.isEmpty() ? factory.get() : pool.poll();
    }
    
    public void release(T obj) {
        pool.offer(obj);
    }
}

// Usage for particles
private static final ObjectPool<BulletParticle> PARTICLE_POOL = 
    new ObjectPool<>(() -> new BulletParticle(), 100);
```

### 2. Flyweight Pattern
Share common data between similar objects:

```java
public class GunType {
    private final String id;
    private final int maxAmmo;
    private final float damage;
    // Common data shared by all instances
}

public class GunInstance {
    private final GunType type; // Reference to shared data
    private int currentAmmo; // Instance-specific data
}
```

### 3. Primitive Arrays Over Collections
Use primitive arrays for numeric data:

```java
// Bad: List<Double>
List<Double> positions = new ArrayList<>();

// Good: double[]
double[] positions = new double[MAX_POSITIONS * 3]; // x, y, z
```

## CPU Optimization

### 1. Tick Throttling
Execute logic only when needed:

```java
private int tickCounter = 0;

@Override
public void tick() {
    tickCounter++;
    
    // Execute every 20 ticks (1 second)
    if (tickCounter % 20 == 0) {
        updateSlowLogic();
    }
    
    // Execute every 5 ticks
    if (tickCounter % 5 == 0) {
        updateMediumLogic();
    }
    
    // Execute every tick
    updateFastLogic();
}
```

### 2. Lazy Initialization
Initialize expensive objects only when needed:

```java
private LazyOptional<CustomData> customData = LazyOptional.of(() -> {
    // Expensive computation only runs when first accessed
    return computeExpensiveData();
});
```

### 3. Cache Frequently Used Values
Cache results of expensive computations:

```java
public class GunStats {
    private static final Map<String, GunStats> CACHE = new HashMap<>();
    
    private final float effectiveDamage;
    
    public static GunStats get(String gunId, List<GunAttachment> attachments) {
        String cacheKey = gunId + attachments.hashCode();
        return CACHE.computeIfAbsent(cacheKey, 
            k -> new GunStats(gunId, attachments));
    }
}
```

### 4. Avoid Reflection in Hot Paths
Cache reflective lookups:

```java
// Bad: Reflection in hot loop
public void tick() {
    for (Entity e : entities) {
        Method method = e.getClass().getMethod("getData");
        method.invoke(e);
    }
}

// Good: Cache reflection
private static final Method GET_DATA_METHOD = 
    Entity.class.getMethod("getData");

public void tick() {
    for (Entity e : entities) {
        GET_DATA_METHOD.invoke(e);
    }
}
```

## Rendering Optimization

### 1. Batch Rendering
Group similar render calls:

```java
public void renderBullets(PoseStack poseStack, MultiBufferSource bufferSource, 
                        List<Bullet> bullets) {
    VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
    
    for (Bullet bullet : bullets) {
        // Add all vertices before calling endBatch
        buffer.vertex(...);
        buffer.vertex(...);
    }
    
    // Single draw call for all bullets
}
```

### 2. Distance Culling
Don't render what you can't see:

```java
public void renderEffects(PoseStack poseStack, Vec3 cameraPos, List<Effect> effects) {
    final double MAX_DISTANCE = 64.0;
    final double MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;
    
    for (Effect effect : effects) {
        double distSq = cameraPos.distanceToSqr(effect.x, effect.y, effect.z);
        if (distSq < MAX_DISTANCE_SQ) {
            effect.render(poseStack);
        }
    }
}
```

### 3. Level of Detail (LOD)
Use simpler models at distance:

```java
public Model getModelForDistance(double distance) {
    if (distance < 16.0) {
        return highDetailModel;
    } else if (distance < 64.0) {
        return mediumDetailModel;
    } else {
        return lowDetailModel;
    }
}
```

### 4. Instanced Rendering
Render multiple instances efficiently:

```java
// For bullet tracers or particles
public void renderInstanced(PoseStack poseStack, List<Vec3> positions) {
    // Use instanced rendering if available
    // or batch all positions into single draw call
}
```

## Multithreading

### 1. Async Pathfinding
```java
public CompletableFuture<Path> findPathAsync(BlockPos start, BlockPos end) {
    return CompletableFuture.supplyAsync(() -> {
        return findPath(start, end); // Computationally expensive
    }, pathfindingExecutor);
}

// Usage
findPathAsync(from, to).thenAccept(path -> {
    // Run on main thread after completion
    Minecraft.getInstance().execute(() -> {
        displayPath(path);
    });
});
```

### 2. Thread-Safe Data Structures
Use concurrent collections when needed:

```java
private final ConcurrentLinkedQueue<Bullet> bulletsToAdd = new ConcurrentLinkedQueue<>();

public void onBulletFired(Bullet bullet) {
    bulletsToAdd.offer(bullet);
}

public void tick() {
    Bullet bullet;
    while ((bullet = bulletsToAdd.poll()) != null) {
        // Add to main list on main thread
        level.addFreshEntity(bullet);
    }
}
```

### 3. Avoid Blocking Main Thread
Never block main thread:

```java
// Bad: Blocks main thread
public void tick() {
    Path path = findPathAsync(from, to).get(); // BLOCKS!
}

// Good: Async callback
public void tick() {
    if (currentPath == null) {
        findPathAsync(from, to).thenAccept(path -> {
            Minecraft.getInstance().execute(() -> {
                currentPath = path;
            });
        });
    }
}
```

## Algorithm Optimization

### 1. Spatial Partitioning
Use octree or spatial hash for fast queries:

```java
public class SpatialHash {
    private final Map<Long, List<Entity>> cells = new HashMap<>();
    private final double cellSize;
    
    public void insert(Entity entity) {
        long cellKey = getCellKey(entity.position());
        cells.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(entity);
    }
    
    public List<Entity> query(Vec3 pos, double radius) {
        List<Entity> results = new ArrayList<>();
        long centerKey = getCellKey(pos);
        
        // Check nearby cells
        for (long key : getNearbyCells(centerKey, radius)) {
            List<Entity> cell = cells.get(key);
            if (cell != null) {
                results.addAll(cell);
            }
        }
        
        return results;
    }
}
```

### 2. Fast Distance Checks
Use squared distances when possible:

```java
// Bad: Square root is expensive
if (Math.sqrt(dx*dx + dy*dy + dz*dz) < 10.0) { }

// Good: Compare squared distances
final double THRESHOLD_SQ = 10.0 * 10.0;
if (dx*dx + dy*dy + dz*dz < THRESHOLD_SQ) { }
```

### 3. Optimized A* Pathfinding
```java
public class OptimizedAStar {
    // Use binary heap for open set
    private final PriorityQueue<Node> openSet;
    // Use HashMap for closed set (O(1) lookup)
    private final Map<Long, Node> closedSet;
    
    public Path findPath(BlockPos start, BlockPos end) {
        openSet.clear();
        closedSet.clear();
        
        Node startNode = new Node(start, 0, heuristic(start, end));
        openSet.offer(startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.pos.equals(end)) {
                return reconstructPath(current);
            }
            
            closedSet.put(encodePos(current.pos), current);
            
            for (Node neighbor : getNeighbors(current)) {
                if (closedSet.containsKey(encodePos(neighbor.pos))) {
                    continue;
                }
                
                // Check if better path exists
                // ...
            }
        }
        
        return null; // No path found
    }
    
    // Octile heuristic for 3D
    private double heuristic(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        int dz = Math.abs(a.getZ() - b.getZ());
        return Math.sqrt(2) * Math.min(dx, dy) + Math.max(dx, dy) + dz;
    }
}
```

## Data Structure Optimization

### 1. Coordinate Compression
Compress 3D coordinates to single long:

```java
public static long encodeBlockPos(int x, int y, int z) {
    return ((long)x & 0x3FFFFFF) << 38 | 
           ((long)z & 0x3FFFFFF) << 12 | 
           ((long)y & 0xFFF);
}

public static BlockPos decodeBlockPos(long value) {
    int x = (int)(value >> 38);
    int y = (int)(value & 0xFFF);
    int z = (int)((value >> 12) & 0x3FFFFFF);
    return new BlockPos(x, y, z);
}
```

### 2. Bit Manipulation for State
Use bits for boolean flags:

```java
public class GunState {
    // Bit 0: isFiring
    // Bit 1: isReloading
    // Bit 2: isScoped
    private int flags = 0;
    
    public boolean isFiring() { return (flags & 0x1) != 0; }
    public void setFiring(boolean firing) { 
        flags = firing ? flags | 0x1 : flags & ~0x1; 
    }
    
    public boolean isReloading() { return (flags & 0x2) != 0; }
    public void setReloading(boolean reloading) { 
        flags = reloading ? flags | 0x2 : flags & ~0x2; 
    }
}
```

## Profiling Guidelines

### 1. Use Spark Profiler
```java
// Add profiling markers
SparkProfiler.startSection("gun_firing");
// ... gun firing code
SparkProfiler.endSection();
```

### 2. Monitor Key Metrics
- FPS (frames per second)
- ms/tick (server tick time)
- Memory usage
- Draw calls
- Particle count
- Entity count

### 3. Identify Bottlenecks
- High ms/tick → CPU/game logic
- Low FPS → GPU/rendering
- Memory leaks → Object allocation/garbage collection

### 4. Optimization Strategy
1. Profile to find bottleneck
2. Optimize the bottleneck
3. Re-profile to verify improvement
4. Move to next bottleneck
