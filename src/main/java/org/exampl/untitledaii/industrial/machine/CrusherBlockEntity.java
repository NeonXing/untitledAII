package org.exampl.untitledaii.industrial.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.exampl.untitledaii.industrial.ModBlockEntities;
import org.exampl.untitledaii.industrial.machine.recipes.MachineRecipe;
import org.exampl.untitledaii.industrial.machine.recipes.MachineRecipeType;

/**
 * Crusher block entity for crushing ores into ingots.
 *
 * <p>Processing:</p>
 * <ul>
 *   <li>Input slot 0: Ore to crush</li>
 *   <li>Output slot 1: Crushed ingots (2x output)</li>
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

    private MachineRecipe currentRecipe;

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUSHER.get(), pos, state,
            ENERGY_CAPACITY, MAX_ENERGY_RECEIVE, MAX_ENERGY_EXTRACT,
            3, 2); // 3 slots: input, output, upgrade. Upgrade at slot 2

        // Set max process time
        this.maxProcessTime = PROCESS_TIME;
    }

    @Override
    protected int getEnergyPerTick() {
        if (currentRecipe != null) {
            return currentRecipe.getEnergyRequired() / currentRecipe.getProcessTime();
        }
        return ENERGY_PER_TICK;
    }

    @Override
    protected boolean findRecipe() {
        ItemStack input = inventory.getStackInSlot(0);
        if (input.isEmpty()) {
            currentRecipe = null;
            return false;
        }

        if (level == null) {
            currentRecipe = null;
            return false;
        }

        currentRecipe = MachineRecipeType.findRecipe(level, input);
        if (currentRecipe != null) {
            maxProcessTime = currentRecipe.getProcessTime();
        }

        return currentRecipe != null;
    }

    @Override
    protected boolean hasOutputSpace() {
        if (currentRecipe == null) {
            return true;
        }

        ItemStack outputSlot = inventory.getStackInSlot(1);
        if (outputSlot.isEmpty()) {
            return true;
        }

        ItemStack firstOutput = currentRecipe.getOutputs().get(0);
        if (outputSlot.sameItem(firstOutput)) {
            return outputSlot.getCount() + firstOutput.getCount() <= outputSlot.getMaxStackSize();
        }
        return outputSlot.isEmpty();
    }

    @Override
    protected void consumeInputs() {
        ItemStack input = inventory.getStackInSlot(0);
        input.shrink(1);
    }

    @Override
    protected void produceOutputs() {
        if (currentRecipe == null) {
            return;
        }

        for (ItemStack output : currentRecipe.getOutputs()) {
            ItemStack outputSlot = inventory.getStackInSlot(1);
            if (outputSlot.isEmpty()) {
                inventory.setStackInSlot(1, output.copy());
            } else if (outputSlot.sameItem(output)) {
                outputSlot.grow(output.getCount());
            } else {
                inventory.setStackInSlot(1, output.copy());
            }
        }
    }
}
