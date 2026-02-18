package org.exampl.untitledaii.industrial.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.exampl.untitledaii.industrial.machine.upgrades.MachineUpgrade;
import org.exampl.untitledaii.industrial.machine.upgrades.MachineUpgrade.UpgradeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all machine block entities.
 *
 * <p>Provides common machine functionality:</p>
 * <ul>
 *   <li>Energy storage</li>
 *   <li>Item inventory</li>
 *   <li>Recipe processing</li>
 *   <li>Progress tracking</li>
 *   <li>Upgrade system</li>
 * </ul>
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public abstract class BaseMachineBlockEntity extends MachineBlockEntity implements IMachine {

    protected final MachineEnergyStorage energyStorage;
    protected final ItemStackHandler inventory;
    protected int processTime;
    protected int maxProcessTime;
    protected boolean isProcessing;
    protected final int upgradeSlot;

    public BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                  int energyCapacity, int maxEnergyReceive, int maxEnergyExtract,
                                  int inventorySize, int upgradeSlotIndex) {
        super(type, pos, state);
        this.energyStorage = new MachineEnergyStorage(energyCapacity, maxEnergyReceive, maxEnergyExtract,
            this::onEnergyChanged);
        this.inventory = new ItemStackHandler(inventorySize);
        this.upgradeSlot = upgradeSlotIndex;
        this.processTime = 0;
        this.maxProcessTime = 0;
        this.isProcessing = false;
    }

    @Override
    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        updateProcessingState();

        if (canProcess()) {
            process();
        } else {
            resetProgress();
        }
    }

    protected void updateProcessingState() {
        boolean shouldProcess = canProcess();
        if (shouldProcess != isProcessing) {
            isProcessing = shouldProcess;
            setChanged();
        }
    }

    @Override
    public boolean canProcess() {
        return hasValidRecipe() && hasEnoughEnergy() && hasOutputSpace();
    }

    @Override
    public void process() {
        if (!canProcess()) {
            return;
        }

        // Consume energy
        int energyPerTick = getEnergyPerTick();
        if (energyStorage.extractEnergy(energyPerTick, true) < energyPerTick) {
            return;
        }

        energyStorage.extractEnergy(energyPerTick, false);

        // Advance progress
        if (maxProcessTime > 0) {
            processTime++;
        }

        // Check if complete
        if (processTime >= maxProcessTime) {
            completeProcess();
        }

        setChanged();
    }

    protected void completeProcess() {
        // Consume inputs
        consumeInputs();

        // Produce outputs
        produceOutputs();

        // Find next recipe
        findRecipe();

        // Reset progress
        resetProgress();

        setChanged();
    }

    protected void resetProgress() {
        processTime = 0;
        maxProcessTime = 0;
    }

    @Override
    public float getProgress() {
        return maxProcessTime > 0 ? (float) processTime / maxProcessTime : 0.0f;
    }

    @Override
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public IItemHandler getItemHandler() {
        return inventory;
    }

    @Override
    public boolean isProcessing() {
        return isProcessing;
    }

    /**
     * Gets energy consumed per tick.
     *
     * @return Energy in FE/tick
     */
    protected int getEnergyPerTick() {
        return 10;
    }

    /**
     * Finds a valid recipe for current inputs.
     *
     * @return true if recipe found, false otherwise
     */
    protected boolean findRecipe() {
        // Override in subclasses
        return false;
    }

    /**
     * Checks if machine has a valid recipe.
     *
     * @return true if valid recipe exists
     */
    protected boolean hasValidRecipe() {
        return findRecipe();
    }

    /**
     * Checks if machine has enough energy to process.
     *
     * @return true if enough energy
     */
    protected boolean hasEnoughEnergy() {
        int energyPerTick = getEnergyPerTick();
        return energyStorage.getEnergyStored() >= energyPerTick;
    }

    /**
     * Checks if there is space for output items.
     *
     * @return true if output space available
     */
    protected boolean hasOutputSpace() {
        // Override in subclasses based on output slots
        return true;
    }

    /**
     * Consumes input items from inventory.
     */
    protected void consumeInputs() {
        // Override in subclasses
    }

    /**
     * Produces output items to inventory.
     */
    protected void produceOutputs() {
        // Override in subclasses
    }

    protected void onEnergyChanged() {
        setChanged();
    }

    /**
     * Gets speed modifier from upgrades.
     *
     * @return Speed multiplier (1.0 = normal)
     */
    protected float getSpeedModifier() {
        float modifier = 1.0f;
        ItemStack upgradeStack = inventory.getStackInSlot(upgradeSlot);
        if (!upgradeStack.isEmpty() && upgradeStack.getItem() instanceof MachineUpgrade) {
            MachineUpgrade.UpgradeType type = ((MachineUpgrade) upgradeStack.getItem()).getType();
            if (type == MachineUpgrade.UpgradeType.SPEED_UPGRADE) {
                modifier += type.getSpeedModifier() * upgradeStack.getCount();
            }
        }
        return modifier;
    }

    /**
     * Gets energy modifier from upgrades.
     *
     * @return Energy modifier (1.0 = normal, <1.0 = more efficient)
     */
    protected float getEnergyModifier() {
        float modifier = 1.0f;
        ItemStack upgradeStack = inventory.getStackInSlot(upgradeSlot);
        if (!upgradeStack.isEmpty() && upgradeStack.getItem() instanceof MachineUpgrade) {
            MachineUpgrade.UpgradeType type = ((MachineUpgrade) upgradeStack.getItem()).getType();
            if (type == MachineUpgrade.UpgradeType.ENERGY_UPGRADE) {
                modifier += type.getEnergyModifier() * upgradeStack.getCount();
            }
        }
        return modifier;
    }

    @Override
    protected void updateProcessingState() {
        boolean shouldProcess = canProcess();
        float speedModifier = getSpeedModifier();
        
        if (shouldProcess != isProcessing) {
            isProcessing = shouldProcess;
            setChanged();
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorage.getCapability(cap, side);
        }
        if (cap == ForgeCapabilities.ITEMS) {
            return LazyOptional.of(() -> inventory).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        energyStorage.invalidate();
        super.invalidateCaps();
    }
}
