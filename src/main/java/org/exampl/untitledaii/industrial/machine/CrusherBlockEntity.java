package org.exampl.untitledaii.industrial.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.exampl.untitledaii.industrial.ModBlockEntities;

/**
 * Crusher block entity for crushing ores into dust.
 *
 * <p>Processing:</p>
 * <ul>
 *   <li>Input slot 0: Ore to crush</li>
 *   <li>Output slot 1: Crushed dust (2x output)</li>
 *   <li>Process time: 100 ticks (5 seconds)</li>
 *   <li>Energy: 20 FE/tick</li>
 * </ul>
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class CrusherBlockEntity extends BaseMachineBlockEntity {

    private static final int ENERGY_CAPACITY = 10000;
    private static final int MAX_ENERGY_RECEIVE = 100;
    private static final int MAX_ENERGY_EXTRACT = 0; // Machines don't output energy
    private static final int PROCESS_TIME = 100;
    private static final int ENERGY_PER_TICK = 20;

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER.get(), pos, state,
            ENERGY_CAPACITY, MAX_ENERGY_RECEIVE, MAX_ENERGY_EXTRACT,
            2); // 2 slots: input and output

        // Set max process time
        this.maxProcessTime = PROCESS_TIME;
    }

    @Override
    protected int getEnergyPerTick() {
        return ENERGY_PER_TICK;
    }

    @Override
    protected boolean findRecipe() {
        ItemStack input = inventory.getStackInSlot(0);
        if (input.isEmpty()) {
            return false;
        }

        // Simple ore crushing recipes (can be expanded)
        String itemName = input.getItem().toString().toLowerCase();
        boolean isValidOre = itemName.contains("ore") ||
                            itemName.contains("raw");

        if (isValidOre) {
            return true;
        }

        return false;
    }

    @Override
    protected boolean hasOutputSpace() {
        ItemStack outputSlot = inventory.getStackInSlot(1);
        if (outputSlot.isEmpty()) {
            return true;
        }
        return outputSlot.getCount() < outputSlot.getMaxStackSize();
    }

    @Override
    protected void consumeInputs() {
        ItemStack input = inventory.getStackInSlot(0);
        input.shrink(1);
    }

    @Override
    protected void produceOutputs() {
        ItemStack input = inventory.getStackInSlot(0);
        String itemName = input.getItem().toString().toLowerCase();

        // Determine output based on input
        ItemStack output;
        if (itemName.contains("iron")) {
            output = new ItemStack(net.minecraft.world.level.block.Blocks.IRON_BLOCK, 2);
        } else if (itemName.contains("gold")) {
            output = new ItemStack(net.minecraft.world.level.block.Blocks.GOLD_BLOCK, 2);
        } else if (itemName.contains("copper")) {
            output = new ItemStack(net.minecraft.world.level.block.Blocks.COPPER_BLOCK, 2);
        } else {
            output = new ItemStack(net.minecraft.world.item.Items.GRAVEL, 2);
        }

        // Add to output slot
        ItemStack outputSlot = inventory.getStackInSlot(1);
        if (outputSlot.isEmpty()) {
            inventory.setStackInSlot(1, output.copy());
        } else if (outputSlot.sameItem(output)) {
            outputSlot.grow(output.getCount());
        }
    }
}
