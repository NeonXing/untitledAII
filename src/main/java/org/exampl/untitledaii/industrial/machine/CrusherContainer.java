package org.exampl.untitledaii.industrial.machine;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.exampl.untitledaii.industrial.ModContainers;

/**
 * Container for Crusher machine GUI.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class CrusherContainer extends AbstractContainerMenu {

    private final CrusherBlockEntity blockEntity;
    private final ContainerLevelAccess levelAccess;

    public CrusherContainer(int id, Inventory inv, CrusherBlockEntity te) {
        super(ModContainers.CRUSHER.get(), id);
        this.blockEntity = te;
        this.levelAccess = ContainerLevelAccess.create(te.getLevel(), te.getBlockPos());

        // Player inventory slots (bottom row)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 84 + 20));
        }

        // Player inventory slots (hotbar)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inv, i, 8 + i * 18, 142));
        }

        // Crusher input slot
        this.addSlot(new CrusherInputSlot(te.getInventory(), 0, 56, 17));

        // Crusher output slot
        this.addSlot(new CrusherOutputSlot(te.getInventory(), 1, 56, 53));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.levelAccess.evaluate((level, pos) -> {
            return level.getBlockEntity(pos) == this.blockEntity;
        });
    }

    @Override
    public ItemStack quickMoveStack(ItemStack stack, int slot) {
        // Move items from crusher output to player inventory
        if (slot == 1) {
            return this.moveItemStackToPlayerInventory(stack);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack moveItemStackToPlayerInventory(ItemStack stack) {
        for (int i = 0; i < 9; i++) {
            Slot slot = this.slots.get(i);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Input slot - only allows input, not extraction.
     */
    public static class CrusherInputSlot extends Slot {
        public CrusherInputSlot(net.minecraftforge.items.IItemHandler itemHandler, int slot, int x, int y) {
            super(itemHandler, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    /**
     * Output slot - only allows extraction, not input.
     */
    public static class CrusherOutputSlot extends Slot {
        public CrusherOutputSlot(net.minecraftforge.items.IItemHandler itemHandler, int slot, int x, int y) {
            super(itemHandler, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
