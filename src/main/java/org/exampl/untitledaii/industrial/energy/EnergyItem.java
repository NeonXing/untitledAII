package org.exampl.untitledaii.industrial.energy;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Base class for energy-containing items like batteries and energy cells.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class EnergyItem extends Item implements ICapabilityProvider {

    private final IEnergyStorage energyStorage;

    /**
     * Creates an energy item with specified capacity.
     *
     * @param properties Item properties
     * @param capacity Energy capacity (FE)
     */
    public EnergyItem(Properties properties, int capacity) {
        this(properties, capacity, capacity, capacity);
    }

    /**
     * Creates an energy item with specified limits.
     *
     * @param properties Item properties
     * @param capacity Maximum energy capacity (FE)
     * @param maxReceive Maximum energy receive rate (FE/tick)
     * @param maxExtract Maximum energy extract rate (FE/tick)
     */
    public EnergyItem(Properties properties, int capacity, int maxReceive, int maxExtract) {
        super(properties);
        this.energyStorage = new ItemEnergyStorage(capacity, maxReceive, maxExtract);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.min(13, energyStorage.getEnergyStored() * 13.0 / energyStorage.getMaxEnergyStored());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFFF00;
    }

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, net.minecraft.core.Direction side) {
        if (cap == net.minecraftforge.common.ForgeHooks.ENERGY_CAPABILITY) {
            return net.minecraftforge.common.util.LazyOptional.of(() -> energyStorage).cast();
        }
        return net.minecraftforge.common.util.LazyOptional.empty();
    }

    /**
     * Energy storage implementation for items.
     */
    private static class ItemEnergyStorage implements IEnergyStorage {
        private int energy;
        private final int capacity;
        private final int maxReceive;
        private final int maxExtract;

        public ItemEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            this.capacity = capacity;
            this.maxReceive = maxReceive;
            this.maxExtract = maxExtract;
            this.energy = 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = Math.min(maxReceive, Math.min(this.maxReceive, capacity - energy));
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

        @Override
        public int canExtract() {
            return energy > 0 ? 1 : 0;
        }

        @Override
        public int canReceive() {
            return energy < capacity ? 1 : 0;
        }
    }
}
