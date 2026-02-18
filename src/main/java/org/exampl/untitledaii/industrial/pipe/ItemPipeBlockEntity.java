package org.exampl.untitledaii.industrial.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.exampl.untitledaii.industrial.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Item pipe block entity for transporting items.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class ItemPipeBlockEntity extends BlockEntity implements IPipe {

    private static final int TRANSFER_COOLDOWN = 20;
    private final ItemStackHandler itemHandler;
    private final LazyOptional<IItemHandler> itemHandlerCap;
    private int transferCooldown;

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_PIPE.get(), pos, state);
        this.itemHandler = new ItemStackHandler(1);
        this.itemHandlerCap = LazyOptional.of(() -> itemHandler);
        this.transferCooldown = 0;
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (transferCooldown > 0) {
            transferCooldown--;
            return;
        }

        ItemStack stack = itemHandler.getStackInSlot(0);
        if (stack.isEmpty()) {
            return;
        }

        // Try to transfer to connected blocks
        Direction facing = getBlockState().getValue(ItemPipeBlock.FACING);
        BlockPos targetPos = getBlockPos().relative(facing);
        
        level.getCapability(ForgeCapabilities.ITEMS, targetPos, facing.getOpposite())
            .ifPresent(targetHandler -> {
                int count = Math.min(stack.getCount(), 64);
                ItemStack transferred = transfer(stack, targetHandler, count);
                
                if (!transferred.isEmpty()) {
                    stack.shrink(transferred.getCount());
                    if (stack.isEmpty()) {
                        itemHandler.setStackInSlot(0, ItemStack.EMPTY);
                    }
                    transferCooldown = TRANSFER_COOLDOWN;
                }
            });
    }

    private ItemStack transfer(ItemStack stack, IItemHandler targetHandler, int maxCount) {
        ItemStack toTransfer = stack.copy();
        toTransfer.setCount(Math.min(toTransfer.getCount(), maxCount));
        
        ItemStack remaining = targetHandler.insertItem(0, toTransfer, false);
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEMS) {
            return itemHandlerCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        itemHandlerCap.invalidate();
        super.invalidateCaps();
    }

    @Override
    public PipeType getType() {
        return PipeType.ITEM;
    }

    @Override
    public int insert(Object resource, Direction side) {
        if (resource instanceof ItemStack) {
            ItemStack stack = (ItemStack) resource;
            ItemStack current = itemHandler.getStackInSlot(0);
            if (current.isEmpty()) {
                itemHandler.setStackInSlot(0, stack.copy());
                return stack.getCount();
            } else if (ItemStack.isSameItemSameTags(current, stack)) {
                int maxInsert = Math.min(stack.getCount(), 64 - current.getCount());
                current.grow(maxInsert);
                return maxInsert;
            }
        }
        return 0;
    }

    @Override
    public Object extract(Direction side, int amount) {
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (!stack.isEmpty()) {
            int toExtract = Math.min(stack.getCount(), amount);
            ItemStack extracted = stack.split(toExtract);
            if (stack.isEmpty()) {
                itemHandler.setStackInSlot(0, ItemStack.EMPTY);
            }
            return extracted;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canAccept(Direction side) {
        ItemStack stack = itemHandler.getStackInSlot(0);
        return stack.isEmpty() || stack.getCount() < 64;
    }
}
