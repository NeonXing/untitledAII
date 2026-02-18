---
name: ava-industrial-development
description: This skill should be used when developing industrial and automation features for the AVA Minecraft mod. Covers machine systems, energy networks, processing chains, automation, and借鉴优秀工业模组的设计模式.
---

# AVA Industrial Development Skill

## Purpose

Design and implement industrial systems including machines, energy networks, processing chains, and automation features, following best practices from top industrial mods like IC2, Mekanism, Thermal Expansion, and Create.

## When to Use

Use this skill when:
- Designing machine systems
- Implementing energy/RF networks
- Creating processing chains (ore processing)
- Building automation systems
- Designing pipe/conduit systems
- Implementing recipe systems

## Excellent Industrial Mods to Reference

### IndustrialCraft 2 (IC2)
- **EU Energy System** - Standardized energy unit
- **Multi-stage Processing** - Macerator → Furnace → Purification
- **Battery System** - Charge/discharge mechanics
- **Reference:** https://github.com/minecraft-mod/industrialcraft2

### Thermal Series
- **RF (Redstone Flux)** - Became the standard energy system
- **Clean Machine Design** - Simple and extensible
- **Recipe Management** - Clear recipe registration
- **Reference:** https://github.com/CoFH/ThermalSeries

### Mekanism
- **Hydrogen Gas System** - Electrolysis → Hydrogen
- **Fluid Pipe Network** - Sophisticated fluid transport
- **Ore Tripling** - 1 ore → 3 ingots
- **Reference:** https://github.com/mekanism/Mekanism

### Create
- **Mechanical Power** - Shaft wheels, belts
- **Conveyor Belts** - Item transport
- **Physics Animations** - Smooth mechanical animations
- **Reference:** https://github.com/Creators-of-Create/Create

### Immersive Engineering
- **Realistic Structures** - Cranes, windmills, excavators
- **Wire Networks** - Realistic energy transmission
- **Hydraulics** - Fluid pressure systems
- **Reference:** https://github.com/BluSunrize/ImmersiveEngineering

### Applied Energistics 2 (AE2)
- **ME Storage System** - Advanced item storage
- **Terminals** - GUI for accessing storage
- **Auto-crafting** - Automated crafting chains
- **Reference:** https://github.com/AppliedEnergistics/Applied-Energistics-2

### Ender IO
- **Unified Conduits** - Energy, item, fluid in one block
- **Simple Machines** - Clean machine design
- **Reference:** https://github.com/EndlessIO/EndlessIO

## Energy System Design

### Use Forge Energy API (FE)

```java
public class MachineEnergyStorage implements IEnergyStorage {
    private int energy;
    private final int capacity;
    private final int maxReceive;
    private final int maxExtract;
    
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.min(maxReceive, Math.min(this.maxReceive, 
                         capacity - energy));
        if (!simulate) {
            energy += received;
        }
        return received;
    }
    
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(maxExtract, Math.min(this.maxExtract, energy));
        if (!simulate) {
            energy -= extracted;
        }
        return extracted;
    }
    
    @Override
    public int getEnergyStored() {
        return energy;
    }
    
    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }
}
```

### Capability Registration

```java
public class MachineBlockEntity extends BlockEntity {
    private final MachineEnergyStorage energyStorage = 
        new MachineEnergyStorage(10000, 100, 100);
    
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, 
                                             Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return LazyOptional.of(() -> energyStorage).cast();
        }
        return super.getCapability(cap, side);
    }
}
```

## Machine System Design

### Machine Interface

```java
public interface IMachine {
    /**
     * Check if machine can process
     */
    boolean canProcess();
    
    /**
     * Process one tick
     */
    void process();
    
    /**
     * Get progress (0.0 - 1.0)
     */
    float getProgress();
    
    /**
     * Get energy storage
     */
    IEnergyStorage getEnergyStorage();
    
    /**
     * Get item handler
     */
    IItemHandler getItemHandler();
}
```

### Machine Block Entity

```java
public class MachineBlockEntity extends BlockEntity 
    implements TickableBlockEntity, IMachine {
    
    private final MachineEnergyStorage energyStorage;
    private final ItemStackHandler inventory;
    private MachineRecipe currentRecipe;
    private int processTime;
    private int maxProcessTime;
    
    @Override
    public void tick() {
        if (canProcess()) {
            process();
        }
    }
    
    @Override
    public boolean canProcess() {
        if (currentRecipe == null) {
            currentRecipe = findRecipe();
        }
        return currentRecipe != null && 
               energyStorage.getEnergyStored() >= currentRecipe.getEnergyRequired();
    }
    
    @Override
    public void process() {
        energyStorage.extractEnergy(1, false);
        processTime++;
        
        if (processTime >= maxProcessTime) {
            completeProcess();
        }
    }
    
    private void completeProcess() {
        // Consume inputs, produce outputs
        // Reset process state
        setChanged();
    }
}
```

## Recipe System Design

### Recipe Definition

```java
public class MachineRecipe {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final NonNullList<ItemStack> outputs;
    private final int processTime;
    private final int energyRequired;
    
    public boolean matches(NonNullList<ItemStack> stacks) {
        // Check if inputs match recipe
        return true;
    }
}
```

### Recipe Registration (JSON-based)

```json
// src/main/resources/data/untitledaii/recipes/machine.json
{
  "type": "untitledaii:machine",
  "inputs": [
    {
      "item": "minecraft:iron_ore"
    }
  ],
  "outputs": [
    {
      "item": "minecraft:iron_ingot",
      "count": 2
    }
  ],
  "process_time": 100,
  "energy_required": 500
}
```

### Recipe Manager

```java
public class MachineRecipeManager {
    private static final Map<ResourceLocation, MachineRecipe> RECIPES = 
        new HashMap<>();
    
    public static void register(ResourceLocation id, MachineRecipe recipe) {
        RECIPES.put(id, recipe);
    }
    
    public static MachineRecipe findRecipe(IItemHandler inventory) {
        NonNullList<ItemStack> inputs = getInputs(inventory);
        return RECIPES.values().stream()
            .filter(r -> r.matches(inputs))
            .findFirst()
            .orElse(null);
    }
}
```

## Processing Chain Design

### Multi-stage Ore Processing

```java
// Example: Iron Ore Processing Chain
// 1. Iron Ore → Macerator → Crushed Iron Ore (2x)
// 2. Crushed Iron Ore → Furnace → Iron Ingot (2x)
// 3. Iron Ingot → Purifier → Refined Iron (1x, but higher quality)
// 4. Refined Iron → Magnification Furnace → Iron Ingot (2x)
// Total: 1 Iron Ore → 4 Iron Ingots

public class OreProcessingChain {
    public static void registerRecipes() {
        registerMaceratorRecipe();
        registerFurnaceRecipe();
        registerPurifierRecipe();
        registerMagnificationRecipe();
    }
}
```

## Pipe/Conduit System Design

### Unified Pipe Interface

```java
public interface IPipe {
    enum PipeType { ENERGY, ITEM, FLUID, GAS }
    
    PipeType getType();
    int insert(Object resource, Direction side);
    Object extract(Direction side, int amount);
}

public class ConduitBlock extends Block {
    private final Map<PipeType, IPipe> pipes = new EnumMap<>(PipeType.class);
    
    // Supports multiple types in one block
    public void addPipe(PipeType type, IPipe pipe) {
        pipes.put(type, pipe);
    }
}
```

## GUI Design

### Container

```java
public class MachineContainer extends AbstractContainerMenu {
    private final MachineBlockEntity tileEntity;
    
    public MachineContainer(int id, Inventory inv, MachineBlockEntity te) {
        super(Containers.MACHINE.get(), id);
        this.tileEntity = te;
        
        // Player inventory
        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }
        
        // Machine slots
        addSlot(new MachineSlot(te.getInventory(), 0, 56, 17));
        addSlot(new MachineSlot(te.getInventory(), 1, 56, 53));
    }
}
```

### Screen with Progress Bar

```java
public class MachineScreen extends AbstractContainerScreen<MachineContainer> {
    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, 
                          int mouseX, int mouseY) {
        // Draw progress arrow
        int progress = (int)(tileEntity.getProgress() * 22);
        blit(poseStack, x + 79, y + 34, 176, 14, progress, 16);
        
        // Draw energy bar
        int energyHeight = (int)(tileEntity.getEnergyStored() / 
                               (float)tileEntity.getMaxEnergyStored() * 48);
        blit(poseStack, x + 157, y + 68 - energyHeight, 
              176, 68 - energyHeight, 14, energyHeight);
    }
}
```

## Automation Features

### Conveyor Belt (Like Create)

```java
public class ConveyorBeltBlockEntity extends BlockEntity {
    @Override
    public void tick() {
        // Move items on belt
        Level level = getLevel();
        BlockPos pos = getBlockPos();
        
        // Check for items above belt
        List<Entity> entities = level.getEntitiesOfClass(ItemEntity.class, 
            new AABB(pos).inflate(0.5));
        
        for (Entity entity : entities) {
            // Move entity in belt direction
            Direction facing = getBlockState().getValue(FACING);
            Vec3 movement = Vec3.atLowerCornerOf(facing.getNormal()).scale(0.05);
            entity.setPos(entity.getX() + movement.x, 
                        entity.getY() + movement.y, 
                        entity.getZ() + movement.z);
        }
    }
}
```

### Auto-crafting (Like AE2)

```java
public class AutoCraftingManager {
    public void craft(ResourceLocation recipeId, int amount) {
        MachineRecipe recipe = MachineRecipeManager.get(recipeId);
        
        for (Ingredient ingredient : recipe.getInputs()) {
            // Find ingredients from storage
            ItemStack ingredientStack = findIngredient(ingredient);
            if (ingredientStack.isEmpty()) {
                // Auto-craft ingredient recursively
                craftIngredient(ingredient);
            }
        }
        
        // Execute crafting
        executeCrafting(recipe);
    }
}
```

## Machine Upgrades

### Upgrade System

```java
public enum MachineUpgrade {
    SPEED_UPGRADE("speed", 1.5f),      // 50% faster
    ENERGY_UPGRADE("energy", 0.8f),   // 20% more efficient
    OUTPUT_UPGRADE("output", 1.0f);    // 100% output boost
    
    private final String id;
    private final float modifier;
}

public class MachineBlockEntity {
    private final Set<MachineUpgrade> upgrades = new HashSet<>();
    
    public float getSpeedModifier() {
        return upgrades.stream()
            .filter(u -> u == MachineUpgrade.SPEED_UPGRADE)
            .count() * 0.5f + 1.0f;
    }
    
    public float getEnergyModifier() {
        return upgrades.stream()
            .filter(u -> u == MachineUpgrade.ENERGY_UPGRADE)
            .count() * -0.2f + 1.0f;
    }
}
```

## Performance Optimization

### Lazy Loading

```java
public class MachineBlockEntity {
    private MachineRecipe cachedRecipe;
    private long lastRecipeCheckTime;
    
    private MachineRecipe findRecipe() {
        long now = System.currentTimeMillis();
        if (cachedRecipe == null || now - lastRecipeCheckTime > 5000) {
            cachedRecipe = MachineRecipeManager.findRecipe(inventory);
            lastRecipeCheckTime = now;
        }
        return cachedRecipe;
    }
}
```

### Distance Culling

```java
public class MachineBlockEntity {
    private static final int MAX_RENDER_DISTANCE = 64;
    
    public boolean shouldRender(BlockPos pos, Player player) {
        return player.blockPosition().distSqr(pos) < 
               MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE;
    }
}
```

## Best Practices

1. **Use Forge Energy API** - Standard energy system for compatibility
2. **Implement Capabilities** - For energy, items, fluids
3. **JSON-based Recipes** - Support datapacks
4. **Proper GUI Design** - Progress bars, energy indicators
5. **Network Sync** - Sync machine state to clients
6. **Lazy Loading** - Cache recipes and expensive calculations
7. **Distance Culling** - Don't tick/render distant machines
8. **Object Pooling** - Reuse objects where possible

## Project Structure

```
src/main/java/org/example/untitledaii/industrial/
├── energy/
│   ├── EnergyStorage.java
│   ├── EnergyNetwork.java
│   └── EnergyCapProvider.java
├── machine/
│   ├── IMachine.java
│   ├── MachineBlock.java
│   ├── MachineBlockEntity.java
│   ├── MachineContainer.java
│   ├── MachineScreen.java
│   ├── upgrades/
│   │   └── MachineUpgrade.java
│   └── recipes/
│       ├── MachineRecipe.java
│       └── MachineRecipeManager.java
├── pipe/
│   ├── IPipe.java
│   ├── PipeBlock.java
│   ├── PipeBlockEntity.java
│   └── PipeNetwork.java
├── conveyor/
│   ├── ConveyorBeltBlock.java
│   └── ConveyorBeltBlockEntity.java
└── automation/
    └── AutoCraftingManager.java
```

## References

- `.codebuddy/references/industrial-mods-research.md` - Detailed mod research
- `.codebuddy/skills/ava-performance-tuning` - Performance optimization
- `.codebuddy/skills/ava-rendering-system` - Machine animations
