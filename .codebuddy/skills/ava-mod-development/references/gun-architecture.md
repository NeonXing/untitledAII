# Gun Architecture Reference

## Core Gun Data Structure

```java
public class GunData {
    private final String id;
    private final int maxAmmo;
    private final int fireRate; // ticks between shots
    private final float damage;
    private final float spread;
    private final int reloadTime; // ticks
    private final boolean automatic;
    private final List<GunAttachment> compatibleAttachments;
    
    // Attachment slots
    private AttachmentSlot scopeSlot;
    private AttachmentSlot muzzleSlot;
    private AttachmentSlot magazineSlot;
    private AttachmentSlot stockSlot;
}

public class GunState {
    private int currentAmmo;
    private boolean isReloading;
    private int reloadProgress;
    private int fireCooldown;
    private GunAttachment equippedScope;
    private GunAttachment equippedMuzzle;
    private GunAttachment equippedMagazine;
    private GunAttachment equippedStock;
}
```

## Event Flow for Firing

1. **PlayerInteractEvent** (Right-click)
   - Check gun is held
   - Check cooldown is zero
   - Check ammo > 0
   - Check not reloading

2. **Fire Bullet**
   - Spawn bullet entity
   - Apply spread from attachments and movement
   - Reduce ammo
   - Set cooldown
   - Play sound
   - Spawn muzzle flash particles

3. **Bullet Tick**
   - Move forward
   - Check collision with entities/blocks
   - Apply damage on hit
   - Spawn hit particles
   - Remove bullet

4. **Render Events**
   - Render gun in hand (first person)
   - Render bullet trail (tracer)
   - Render muzzle flash
   - Render hit effects

## Attachment System Architecture

```java
public class GunAttachment {
    private final String id;
    private final AttachmentType type;
    private final Map<String, Float> modifiers;
    
    // Modifiers
    private Float damageModifier;
    private Float spreadModifier;
    private Float fireRateModifier;
    private Float reloadSpeedModifier;
    private Float recoilModifier;
}

public enum AttachmentType {
    SCOPE, MUZZLE, MAGAZINE, STOCK, GRIP, LASER
}
```

## Rendering Pipeline

### First Person
1. `IClientItemExtensions` provides `BlockEntityWithoutLevelRenderer`
2. Render gun model with attachment modifications
3. Apply animation (recoil, reload)
4. Render scope overlay if equipped

### Third Person
1. Render simplified gun model on player
2. Apply player's arm animation
3. Distance culling for far players

### GUI
1. Render preview in inventory
2. Show attachment slots
3. Display stats (damage, fire rate, etc.)

## Performance Optimization Patterns

### Object Pooling for Particles
```java
public class ParticlePool {
    private final Queue<MuzzleFlashParticle> pool = new ArrayDeque<>();
    
    public MuzzleFlashParticle obtain(Level level, double x, double y, double z) {
        if (pool.isEmpty()) {
            return new MuzzleFlashParticle(level, x, y, z);
        }
        return pool.poll().reset(x, y, z);
    }
    
    public void recycle(MuzzleFlashParticle particle) {
        pool.offer(particle);
    }
}
```

### Batch Tracer Rendering
```java
public class TracerRenderer {
    private final VertexConsumer buffer;
    
    public void renderTracers(PoseStack poseStack, List<Tracer> tracers) {
        for (Tracer tracer : tracers) {
            buffer.vertex(poseStack.last().pose(), tracer.x1, tracer.y1, tracer.z1)
                  .color(tracer.r, tracer.g, tracer.b, tracer.a)
                  .endVertex();
            buffer.vertex(poseStack.last().pose(), tracer.x2, tracer.y2, tracer.z2)
                  .color(tracer.r, tracer.g, tracer.b, tracer.a)
                  .endVertex();
        }
    }
}
```

## Network Synchronization

### Server to Client
- Bullet spawn (position, velocity)
- Hit confirmation (target, damage)
- Ammo count updates
- Reload state updates

### Client to Server
- Fire action (timestamp for anti-cheat)
- Reload action
- Attachment change

## Data Storage

### Using Capabilities
```java
public class GunCapabilityProvider implements ICapabilitySerializable<NBT> {
    private final LazyOptional<GunState> instance;
    
    public GunCapabilityProvider() {
        this.instance = LazyOptional.of(() -> new GunState());
    }
    
    // ... ICapabilityProvider methods
}
```

## Configuration System

```java
public class GunConfig {
    public static ForgeConfigSpec.DoubleValue GLOBAL_DAMAGE_MULTIPLIER;
    public static ForgeConfigSpec.BooleanValue ENABLE_RECOIL;
    public static ForgeConfigSpec.BooleanValue ENABLE_TRACERS;
    
    public static void register(ModLoadingContext context) {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        builder.push("Gameplay");
        GLOBAL_DAMAGE_MULTIPLIER = builder
            .comment("Global damage multiplier for all guns")
            .defineInRange("damageMultiplier", 1.0, 0.1, 10.0);
        ENABLE_RECOIL = builder
            .comment("Enable recoil effect")
            .define("enableRecoil", true);
        ENABLE_TRACERS = builder
            .comment("Enable bullet tracers")
            .define("enableTracers", true);
        builder.pop();
        
        context.registerConfig(ModConfig.Type.COMMON, builder.build());
    }
}
```

## Common Gun Types

### Pistol
- Semi-automatic
- High fire rate (3-5 ticks)
- Low damage
- Low spread
- Fast reload
- Small magazine

### Rifle
- Semi or automatic
- Medium fire rate (2-4 ticks)
- Medium damage
- Medium spread
- Medium reload
- Medium magazine

### Shotgun
- Semi-automatic
- Medium fire rate (8-12 ticks)
- High damage (multiple pellets)
- High spread
- Slow reload
- Small magazine

### Sniper
- Bolt-action or semi
- Slow fire rate (20-30 ticks)
- Very high damage
- Very low spread
- Medium reload
- Small magazine
