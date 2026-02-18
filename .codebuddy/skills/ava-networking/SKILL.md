---
name: ava-networking
description: This skill should be used when implementing, debugging, or optimizing networking for the AVA Minecraft mod. Covers packet design, client-server synchronization, bandwidth optimization, and network security.
---

# AVA Networking Skill

## Purpose

Design efficient, secure, and reliable networking systems for gun mechanics, synchronization, and multiplayer interactions.

## When to Use

Use this skill when:
- Designing custom packets
- Implementing client-server synchronization
- Debugging network issues
- Optimizing bandwidth usage
- Adding anti-cheat measures

## Packet Design

### Packet Structure

```java
public class GunFirePacket {
    private final int playerId;
    private final String gunId;
    private final float x, y, z;  // Position
    private final float yaw, pitch;  // Direction
}
```

### Packet Registration

```java
public class NetworkHandler {
    private static final SimpleChannel CHANNEL = 
        ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath("untitledaii", "main"))
            .networkProtocolVersion(1)
            .simpleChannel();
    
    public static void register() {
        CHANNEL.messageBuilder(GunFirePacket.class, 0)
            .encoder(GunFirePacket::encode)
            .decoder(GunFirePacket::new)
            .consumerMainThread(GunFirePacket::handle)
            .add();
        
        CHANNEL.messageBuilder(GunReloadPacket.class, 1)
            .encoder(GunReloadPacket::encode)
            .decoder(GunReloadPacket::new)
            .consumerMainThread(GunReloadPacket::handle)
            .add();
    }
}
```

### Packet Encoding/Decoding

```java
public class GunFirePacket {
    // Fields
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(playerId);
        buffer.writeUtf(gunId);
        buffer.writeFloat(x);
        buffer.writeFloat(y);
        buffer.writeFloat(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
    }
    
    public GunFirePacket(FriendlyByteBuf buffer) {
        this.playerId = buffer.readInt();
        this.gunId = buffer.readUtf();
        this.x = buffer.readFloat();
        this.y = buffer.readFloat();
        this.z = buffer.readFloat();
        this.yaw = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }
    
    public void handle(CustomPacketEvent.Context context) {
        context.enqueueWork(() -> {
            // Handle packet on correct side
        });
        context.setPacketHandled(true);
    }
}
```

## Synchronization Patterns

### State Synchronization

```java
// Send only changed fields
public class GunStateSyncPacket {
    private final int playerId;
    private final int ammo;  // Only send if changed
    private final boolean isReloading;
    private final byte flags;  // Bit-packed boolean states
}

// Use bit packing
public static byte packFlags(boolean a, boolean b, boolean c, boolean d) {
    byte flags = 0;
    if (a) flags |= 0x01;
    if (b) flags |= 0x02;
    if (c) flags |= 0x04;
    if (d) flags |= 0x08;
    return flags;
}
```

### Delta Compression

```java
public class PositionPacket {
    private final int playerId;
    private final float deltaX, deltaY, deltaZ;  // Delta from last known
    private final long timestamp;
}
```

### Client Prediction

```java
public class ClientGunHandler {
    private int predictedAmmo;
    private boolean predictedReloading;
    
    public void fireGun() {
        // Predict locally
        predictedAmmo--;
        spawnMuzzleFlash();
        
        // Send to server
        NetworkHandler.sendToServer(new GunFirePacket());
    }
    
    public void onServerUpdate(GunStateSyncPacket packet) {
        // Reconcile with server
        if (packet.getAmmo() != predictedAmmo) {
            predictedAmmo = packet.getAmmo();
            // Handle prediction error
        }
    }
}
```

## Bandwidth Optimization

### Batching

```java
public class GunStateBatcher {
    private final List<GunStateSyncPacket> pending = new ArrayList<>();
    
    public void queue(GunStateSyncPacket packet) {
        pending.add(packet);
        
        if (pending.size() >= 10) {
            flush();
        }
    }
    
    public void flush() {
        if (!pending.isEmpty()) {
            sendBatch(new GunStateBatchPacket(new ArrayList<>(pending)));
            pending.clear();
        }
    }
}
```

### Priority Levels

```java
public enum PacketPriority {
    CRITICAL,   // Immediate: gunfire, hits
    HIGH,       // High: reloads, ammo sync
    MEDIUM,     // Medium: state updates
    LOW         // Low: statistics, telemetry
}
```

### Throttling

```java
public class StateSyncThrottler {
    private long lastSyncTime;
    private static final long SYNC_INTERVAL_MS = 100;  // 10 updates/sec
    
    public boolean shouldSync() {
        long now = System.currentTimeMillis();
        if (now - lastSyncTime >= SYNC_INTERVAL_MS) {
            lastSyncTime = now;
            return true;
        }
        return false;
    }
}
```

## Security

### Server-Side Validation

```java
public class ServerGunHandler {
    public void handleFire(GunFirePacket packet, ServerPlayer player) {
        // Validate packet
        if (!isValidFire(player, packet)) {
            // Anti-cheat: kick or flag
            return;
        }
        
        // Execute fire
        executeFire(player, packet);
    }
    
    private boolean isValidFire(ServerPlayer player, GunFirePacket packet) {
        // Check ammo
        GunState state = getPlayerGunState(player);
        if (state.getCurrentAmmo() <= 0) {
            return false;
        }
        
        // Check cooldown
        if (state.getFireCooldown() > 0) {
            return false;
        }
        
        // Check distance (anti-teleport)
        double distance = player.distanceToSqr(packet.x, packet.y, packet.z);
        if (distance > MAX_VALID_DISTANCE_SQ) {
            return false;
        }
        
        return true;
    }
}
```

### Rate Limiting

```java
public class RateLimiter {
    private final Map<UUID, Long> lastPacketTime = new ConcurrentHashMap<>();
    private final long minIntervalMs;
    
    public RateLimiter(long minIntervalMs) {
        this.minIntervalMs = minIntervalMs;
    }
    
    public boolean allow(UUID playerId) {
        long now = System.currentTimeMillis();
        Long last = lastPacketTime.get(playerId);
        
        if (last == null || now - last >= minIntervalMs) {
            lastPacketTime.put(playerId, now);
            return true;
        }
        return false;
    }
}
```

## Common Patterns

### Request-Response

```java
// Client request
public class GetGunStatePacket {
    public void handle(CustomPacketEvent.Context context) {
        ServerPlayer player = context.getSender();
        GunState state = getPlayerGunState(player);
        
        // Send response
        NetworkHandler.sendToClient(new GunStateResponsePacket(state), player);
    }
}
```

### Broadcast

```java
public class GunFiredBroadcastPacket {
    public static void broadcast(ServerLevel level, Packet packet) {
        for (ServerPlayer player : level.players()) {
            NetworkHandler.sendToClient(packet, player);
        }
    }
}
```

## Debugging

### Packet Logging

```java
public class PacketLogger {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static void logPacket(Packet packet, String direction) {
        LOGGER.debug("{}: {} from {}", 
            direction, 
            packet.getClass().getSimpleName(),
            packet.getSender());
    }
}
```

### Network Monitor

```bash
# View network stats
/netstat client

# Monitor packet flow
/spark netlog 60
```

## Performance Checklist

- [ ] Packets use bit packing for booleans
- [ ] Server validates all inputs
- [ ] Rate limiting implemented
- [ ] Client prediction for smooth gameplay
- [ ] Delta compression for frequent updates
- [ ] Packet batching where appropriate
- [ ] No unnecessary field synchronization

## References

- Forge Network Documentation
- Vanilla Network Code
