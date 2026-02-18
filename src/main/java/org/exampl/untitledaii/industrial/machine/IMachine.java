package org.exampl.untitledaii.industrial.machine;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

/**
 * Interface for all machine block entities.
 *
 * <p>Defines common machine behavior:</p>
 * <ul>
 *   <li>Recipe processing</li>
 *   <li>Energy consumption</li>
 *   <li>Item input/output</li>
 *   <li>Progress tracking</li>
 * </ul>
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public interface IMachine {

    /**
     * Checks if the machine can process current inputs.
     *
     * @return true if processing can begin or continue
     */
    boolean canProcess();

    /**
     * Processes one tick of the recipe.
     */
    void process();

    /**
     * Gets the current processing progress (0.0 to 1.0).
     *
     * @return Progress as a fraction
     */
    float getProgress();

    /**
     * Gets the energy storage of this machine.
     *
     * @return Energy storage capability
     */
    IEnergyStorage getEnergyStorage();

    /**
     * Gets the item handler for this machine.
     *
     * @return Item handler capability
     */
    IItemHandler getItemHandler();

    /**
     * Checks if the machine is currently processing a recipe.
     *
     * @return true if processing, false otherwise
     */
    boolean isProcessing();
}
