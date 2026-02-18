package org.exampl.untitledaii.industrial.energy;

import net.minecraft.core.Direction;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Energy storage implementation for machines using Forge Energy API.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Configurable capacity, receive rate, and extract rate</li>
 *   <li>Energy change callbacks for synchronization</li>
 *   <li>Thread-safe operations</li>
 * </ul>
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class MachineEnergyStorage extends EnergyStorage implements ICapabilityProvider {

    private final Runnable onEnergyChanged;
    private LazyOptional<IEnergyStorage> lazyEnergy;

    /**
     * Creates a machine energy storage with specified limits.
     *
     * @param capacity Maximum energy storage capacity (FE)
     * @param maxReceive Maximum energy that can be received per tick (FE/tick)
     * @param maxExtract Maximum energy that can be extracted per tick (FE/tick)
     */
    public MachineEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        this(capacity, maxReceive, maxExtract, 0);
    }

    /**
     * Creates a machine energy storage with specified limits and initial energy.
     *
     * @param capacity Maximum energy storage capacity (FE)
     * @param maxReceive Maximum energy that can be received per tick (FE/tick)
     * @param maxExtract Maximum energy that can be extracted per tick (FE/tick)
     * @param initialEnergy Initial energy amount
     */
    public MachineEnergyStorage(int capacity, int maxReceive, int maxExtract, int initialEnergy) {
        super(capacity, maxReceive, maxExtract, initialEnergy);
        this.lazyEnergy = LazyOptional.of(() -> this);
        this.onEnergyChanged = null;
    }

    /**
     * Creates a machine energy storage with energy change callback.
     *
     * @param capacity Maximum energy storage capacity (FE)
     * @param maxReceive Maximum energy that can be received per tick (FE/tick)
     * @param maxExtract Maximum energy that can be extracted per tick (FE/tick)
     * @param onEnergyChanged Callback invoked when energy changes
     */
    public MachineEnergyStorage(int capacity, int maxReceive, int maxExtract, Runnable onEnergyChanged) {
        this(capacity, maxReceive, maxExtract, 0, onEnergyChanged);
    }

    /**
     * Creates a machine energy storage with full configuration.
     *
     * @param capacity Maximum energy storage capacity (FE)
     * @param maxReceive Maximum energy that can be received per tick (FE/tick)
     * @param maxExtract Maximum energy that can be extracted per tick (FE/tick)
     * @param initialEnergy Initial energy amount
     * @param onEnergyChanged Callback invoked when energy changes
     */
    public MachineEnergyStorage(int capacity, int maxReceive, int maxExtract, int initialEnergy, Runnable onEnergyChanged) {
        super(capacity, maxReceive, maxExtract, initialEnergy);
        this.lazyEnergy = LazyOptional.of(() -> this);
        this.onEnergyChanged = onEnergyChanged;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0 && onEnergyChanged != null) {
            onEnergyChanged.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0 && onEnergyChanged != null) {
            onEnergyChanged.run();
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return super.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return super.getMaxEnergyStored();
    }

    /**
     * Checks if energy storage is full.
     *
     * @return true if energy equals capacity, false otherwise
     */
    public boolean isFull() {
        return getEnergyStored() >= getMaxEnergyStored();
    }

    /**
     * Checks if energy storage is empty.
     *
     * @return true if energy is 0, false otherwise
     */
    public boolean isEmpty() {
        return getEnergyStored() <= 0;
    }

    /**
     * Gets current energy as a percentage (0.0 to 1.0).
     *
     * @return Energy percentage
     */
    public float getEnergyPercentage() {
        return getMaxEnergyStored() > 0 
            ? (float) getEnergyStored() / getMaxEnergyStored() 
            : 0.0f;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeHooks.ENERGY_CAPABILITY) {
            return lazyEnergy.cast();
        }
        return LazyOptional.empty();
    }

    /**
     * Invalidates the energy capability, should be called when block entity is removed.
     */
    public void invalidate() {
        lazyEnergy.invalidate();
    }
}
