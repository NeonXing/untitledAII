---
name: ava-gun-architecture
description: This skill should be used when designing, implementing, or refactoring the gun system architecture for the AVA Minecraft mod. Covers gun data structures, state management, attachment system, event flow, and performance patterns.
---

# AVA Gun Architecture Skill

## Purpose

Design and implement a high-performance, extensible gun system for the AVA Minecraft mod, following best practices from TACZ and other top-tier gun mods.

## When to Use

Use this skill when:
- Designing new gun data structures (GunData, GunState)
- Implementing gun mechanics (firing, reloading, attachment)
- Refactoring the gun event flow
- Adding attachment compatibility
- Optimizing gun-related performance

## Core Architecture Components

### GunData (Immutable Configuration)

GunData stores immutable gun specifications:

```java
public class GunData {
    private final String gunId;
    private final int maxAmmo;
    private final float fireRate;  // shots per second
    private final float damage;
    private final float spread;
    private final float reloadTime;
    private final boolean isAutomatic;
    private final List<String> compatibleAttachments;
    // ...
}
```

**Design Principles:**
- Immutable - use `static final` instances or cache in registry
- Load from JSON data files in `src/main/resources/data/untitledaii/guns/`
- Validate on registration

### GunState (Mutable Instance State)

GunState tracks per-gun runtime state:

```java
public class GunState {
    private int currentAmmo;
    private boolean isReloading;
    private float reloadProgress;
    private int fireCooldown;
    private Set<String> equippedAttachments;
    // ...
}
```

**Storage:**
- Use Capability system: `IGunStateCapability`
- Sync to clients via NetworkData
- Optimize with packed bits for boolean flags

### Attachment System

```java
public class GunAttachment {
    private final String type;  // "scope", "magazine", "barrel", etc.
    private final Map<String, Float> modifiers;  // damageModifier, spreadModifier, etc.
}
```

**Design Patterns:**
- Strategy pattern for modifier application
- Validate attachment compatibility before equip
- Cache modified gun stats when attachments change

## Event Flow

### Firing Sequence

1. **Client Side:** `PlayerInteractEvent.RightClickBlock`
   - Check fire cooldown
   - Check ammo > 0
   - Send fire packet to server

2. **Server Side:** Validate and execute
   - Check ammo, cooldown, player state
   - Calculate damage with spread
   - Raycast to find hit entity
   - Apply damage to hit entity
   - Decrement ammo
   - Sync state to client
   - Play sound, spawn particles
   - Send fire packet to nearby players

3. **Network:** Custom packet: `GunFirePacket`

### Reloading Flow

1. Start reload: Set `isReloading = true`, `reloadProgress = 0`
2. Each tick: Increment `reloadProgress += reloadTime / 20`
3. Complete: Restore ammo, `isReloading = false`
4. Cancel on fire: Reset reload state

## Performance Optimization

### Object Creation
- Use object pools for bullets, particles
- Cache GunData instances in registry
- Avoid `new Vec3()` in render loops

### Throttling
```java
if (player.tickCount % 2 == 0) {
    // Run every other tick
}
```

### Network Optimization
- Sync only changed state fields
- Use bit packing for booleans
- Batch packet updates when possible

## File Structure

```
src/main/java/org/example/untitledaii/gun/
├── GunData.java
├── GunState.java
├── GunAttachment.java
├── event/ (GunEventHandler.java)
├── capability/ (IGunStateCapability.java, GunStateProvider.java)
├── network/ (GunFirePacket.java, GunReloadPacket.java)
├── registry/ (GunRegistry.java)
└── model/ (GunModelManager.java)

src/main/resources/data/untitledaii/guns/
└── [gun_id].json
```

## Common Patterns

### Data-Driven Gun Registration

```java
public class GunRegistry {
    private static final Map<String, GunData> GUNS = new HashMap<>();
    
    public static void register(ResourceLocation id, GunData data) {
        GUNS.put(id.toString(), data);
    }
    
    public static GunData get(String gunId) {
        return GUNS.get(gunId);
    }
}
```

### Capability-Based State Storage

```java
@CapabilityInject(IGunStateCapability.class)
public static Capability<IGunStateCapability> GUN_STATE_CAP = null;

public class GunStateProvider implements ICapabilitySerializable<CompoundTag> {
    private final LazyOptional<IGunStateCapability> instance;
    // ...
}
```

## Best Practices

1. **Validate input** - Server-side validation for all game-changing actions
2. **Defensive coding** - Null checks, exception handling
3. **Logging** - Use LogUtils for debugging gun events
4. **Testing** - Write GameTest cases for gun mechanics
5. **Performance** - Profile with Spark before optimizing

## References

- `.codebuddy/references/gun-architecture.md` - Detailed architecture specs
- `.codebuddy/references/optimization-techniques.md` - Performance patterns
- TACZ mod source: https://github.com/F1zeiL/TACZ
