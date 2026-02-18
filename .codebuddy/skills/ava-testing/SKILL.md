---
name: ava-testing
description: This skill should be used when writing, running, or debugging tests for the AVA Minecraft mod. Covers unit tests, GameTest framework, integration tests, and testing best practices.
---

# AVA Testing Skill

## Purpose

Create comprehensive tests for gun mechanics, networking, rendering, and performance to ensure mod reliability.

## When to Use

Use this skill when:
- Writing unit tests for game logic
- Creating GameTest for mod features
- Testing networking behavior
- Verifying performance optimizations
- Debugging test failures

## GameTest Framework

### Basic Test Structure

```java
@Gametest(template = "empty")
public class GunMechanicsTest {
    @Test
    public void testGunFiring(TestHelper helper) {
        // Setup
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockPlayer();
        ItemStack gun = new ItemStack(ModItems.AK47.get());
        player.getInventory().add(gun);
        
        // Execute
        GunState state = GunStateProvider.getState(player);
        state.setAmmo(30);
        
        // Fire gun
        GunFireEvent.fire(player, gun);
        
        // Assert
        helper.assert succeeds(() -> {
            assertEquals(29, state.getCurrentAmmo(), "Ammo should decrease by 1");
        });
    }
}
```

### Test Templates

```java
// Use standard templates
@Gametest(template = "flat")  // Flat world
@Gametest(template = "empty") // Empty world
@Gametest(template = "normal") // Normal world

// Or custom structure
@Gametest(template = "my_custom_test_structure")
```

### Assertions

```java
helper.assert succeeds(() -> {
    // Assert conditions
    assertTrue(condition, "Message");
    assertEquals(expected, actual, "Message");
    assertNotNull(value, "Message");
});

// Check block
helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, 1, 0));

// Check entity
helper.assertEntityPresent(EntityType.ZOMBIE, new BlockPos(0, 1, 0));
```

### Test Life Cycle

```java
@Gametest
public class FullGunTest {
    @BeforeEach
    public void setup(TestHelper helper) {
        // Setup before each test
        ServerPlayer player = helper.makeMockPlayer();
        helper.setPlayer(player);
    }
    
    @Test
    public void testFireSequence(TestHelper helper) {
        // Test code
    }
    
    @AfterEach
    public void cleanup(TestHelper helper) {
        // Cleanup after each test
    }
}
```

## Unit Tests

### JUnit Setup

```java
public class GunDataTest {
    private GunData gunData;
    
    @BeforeEach
    public void setup() {
        gunData = new GunData.Builder()
            .gunId("ak47")
            .maxAmmo(30)
            .fireRate(10.0f)
            .build();
    }
    
    @Test
    public void testMaxAmmo() {
        assertEquals(30, gunData.getMaxAmmo());
    }
    
    @Test
    public void testFireRate() {
        assertEquals(10.0f, gunData.getFireRate(), 0.01f);
    }
}
```

### Mock Objects

```java
public class NetworkingTest {
    private MockPacketHandler packetHandler;
    
    @BeforeEach
    public void setup() {
        packetHandler = new MockPacketHandler();
    }
    
    @Test
    public void testPacketEncoding() {
        GunFirePacket packet = new GunFirePacket(1, "ak47", 0, 0, 0, 0, 0);
        
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buffer);
        
        GunFirePacket decoded = new GunFirePacket(buffer);
        assertEquals(packet.getGunId(), decoded.getGunId());
    }
}
```

## Integration Tests

### Multi-Player Test

```java
@Gametest
public class MultiplayerGunTest {
    @Test
    public void testGunFireSync(TestHelper helper) {
        ServerLevel level = helper.getLevel();
        
        // Create two players
        ServerPlayer player1 = helper.makeMockPlayer();
        ServerPlayer player2 = helper.makeMockPlayer();
        
        player1.setPos(0, 1, 0);
        player2.setPos(5, 1, 0);
        
        // Player 1 fires gun
        ItemStack gun = new ItemStack(ModItems.AK47.get());
        GunState state = GunStateProvider.getState(player1);
        state.setAmmo(30);
        
        GunFireEvent.fire(player1, gun);
        
        helper.succeedWhen(() -> {
            // Verify player 2 received sync
            GunState state2 = GunStateProvider.getState(player2);
            assertEquals(29, state2.getCurrentAmmo(), 
                "Player 2 should see updated ammo");
        });
    }
}
```

## Performance Tests

### Benchmarking

```java
@Gametest
public class PerformanceTest {
    @Test
    public void testGunRenderingPerformance(TestHelper helper) {
        long startTime = System.nanoTime();
        
        // Render 100 guns
        for (int i = 0; i < 100; i++) {
            GunModel model = GunModelManager.getModel("ak47");
            model.render(poseStack, buffer, light, overlay);
        }
        
        long duration = System.nanoTime() - startTime;
        long durationMs = duration / 1_000_000;
        
        helper.assert succeeds(() -> {
            assertTrue(durationMs < 100, 
                "Rendering 100 guns should take <100ms, took " + durationMs + "ms");
        });
    }
}
```

### Memory Profiling

```java
@Test
public void testMemoryUsage() {
    long memoryBefore = Runtime.getRuntime().totalMemory() - 
                         Runtime.getRuntime().freeMemory();
    
    // Perform operation
    for (int i = 0; i < 1000; i++) {
        spawnParticle();
    }
    
    long memoryAfter = Runtime.getRuntime().totalMemory() - 
                        Runtime.getRuntime().freeMemory();
    long memoryDelta = memoryAfter - memoryBefore;
    
    assertTrue(memoryDelta < 1024 * 1024, 
        "Memory increase should be <1MB, was " + (memoryDelta / 1024 / 1024) + "MB");
}
```

## Test Organization

### Directory Structure

```
src/test/java/
├── game/
│   ├── gun/
│   │   ├── GunMechanicsTest.java
│   │   ├── GunReloadTest.java
│   │   └── GunAttachmentTest.java
│   ├── networking/
│   │   ├── PacketEncodingTest.java
│   │   └── PacketHandlingTest.java
│   └── performance/
│       ├── RenderingPerformanceTest.java
│       └── TickPerformanceTest.java
└── unit/
    ├── GunDataTest.java
    ├── GunStateTest.java
    └── GunAttachmentTest.java
```

### Test Naming Conventions

```
test[Feature]_When[Condition]_Expect[Result]
testFireGun_WhenHasAmmo_ExpectAmmoDecreases
testReload_WhenEmpty_ExpectFullAmmo
testAttachScope_WhenCompatible_ExpectDamageModifierApplied
```

## Running Tests

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test

```bash
./gradlew test --tests GunMechanicsTest
./gradlew test --tests "*FireGun*"
```

### Run GameTest In-Game

```
/gametest run untitledaii:gun_mechanics
```

## Common Pitfalls

1. **Missing assertions** - Always verify expected behavior
2. **Testing implementation** - Test behavior, not implementation details
3. **Flaky tests** - Use helper.succeedWhen() for async behavior
4. **No cleanup** - Always clean up resources
5. **Over-mocking** - Only mock external dependencies

## Test Checklist

- [ ] Test covers happy path
- [ ] Test covers error cases
- [ ] Test handles edge cases
- [ ] Test is deterministic
- [ ] Test runs quickly (<1s for unit tests)
- [ ] Test has clear assertion messages
- [ ] Test is independent (no shared state)

## References

- Forge GameTest Wiki
- JUnit 5 Documentation
