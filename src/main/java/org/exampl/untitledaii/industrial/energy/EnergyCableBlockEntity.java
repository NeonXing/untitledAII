package org.exampl.untitledaii.industrial.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.exampl.untitledaii.industrial.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Energy cable block entity for transmitting energy between machines.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class EnergyCableBlockEntity extends BlockEntity {

    private static final int TRANSFER_RATE = 1000; // FE per tick
    private final MachineEnergyStorage energyStorage;

    public EnergyCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_CABLE.get(), pos, state);
        this.energyStorage = new MachineEnergyStorage(10000, TRANSFER_RATE, TRANSFER_RATE);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Transfer energy to connected blocks
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = getBlockPos().relative(direction);
            BlockEntity neighborBE = level.getBlockEntity(neighborPos);
            if (neighborBE != null) {
                neighborBE.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite())
                    .ifPresent(neighborStorage -> {
                        int energyToTransfer = Math.min(
                            energyStorage.extractEnergy(TRANSFER_RATE, true),
                            neighborStorage.receiveEnergy(TRANSFER_RATE, true)
                        );
                        if (energyToTransfer > 0) {
                            energyStorage.extractEnergy(energyToTransfer, false);
                            neighborStorage.receiveEnergy(energyToTransfer, false);
                        }
                    });
            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorage.getCapability(cap, side);
        }
        return super.getCapability(cap, side);
    }

    public void invalidateCaps() {
        energyStorage.invalidate();
    }
}
