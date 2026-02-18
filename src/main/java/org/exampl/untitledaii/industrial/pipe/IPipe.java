package org.exampl.untitledaii.industrial.pipe;

import net.minecraft.core.Direction;

/**
 * Interface for pipe/conduit blocks.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public interface IPipe {

    enum PipeType {
        ENERGY, ITEM, FLUID, GAS
    }

    /**
     * Gets the type of this pipe.
     *
     * @return Pipe type
     */
    PipeType getType();

    /**
     * Inserts a resource into this pipe from the given side.
     *
     * @param resource The resource to insert (energy/ItemStack/fluid)
     * @param side The side to insert from
     * @return Amount inserted (may be less than requested)
     */
    int insert(Object resource, Direction side);

    /**
     * Extracts a resource from this pipe from the given side.
     *
     * @param side The side to extract from
     * @param amount Maximum amount to extract
     * @return Extracted resource (may be less than requested)
     */
    Object extract(Direction side, int amount);

    /**
     * Checks if this pipe can accept resources from the given side.
     *
     * @param side The side to check
     * @return true if can accept, false otherwise
     */
    boolean canAccept(Direction side);
}
