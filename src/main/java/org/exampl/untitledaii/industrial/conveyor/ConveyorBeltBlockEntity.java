package org.exampl.untitledaii.industrial.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.exampl.untitledaii.industrial.ModBlocks;
import org.exampl.untitledaii.industrial.ModBlockEntities;

import java.util.List;

/**
 * Conveyor belt block entity for transporting items.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class ConveyorBeltBlockEntity extends BlockEntity {

    private static final float SPEED = 0.1f;
    private static final float OFFSET_Y = 0.1f;

    public ConveyorBeltBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONVEYOR_BELT.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        // Get belt direction
        Direction facing = getBlockState().getValue(ConveyorBeltBlock.FACING);
        Vec3 movement = Vec3.atLowerCornerOf(facing.getNormal()).scale(SPEED);

        // Find items above belt
        List<Entity> entities = level.getEntitiesOfClass(
            ItemEntity.class,
            new AABB(getBlockPos()).inflate(0.5).move(0, OFFSET_Y, 0)
        );

        for (Entity entity : entities) {
            // Move entity in belt direction
            double newX = entity.getX() + movement.x;
            double newZ = entity.getZ() + movement.z;
            double newY = entity.getY();

            // Keep entity on top of belt
            BlockPos entityPos = BlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
            if (entityPos.equals(getBlockPos())) {
                newY = getBlockPos().getY() + OFFSET_Y;
            }

            entity.setPos(newX, newY, newZ);

            // Transfer to next belt if exists
            BlockPos nextPos = getBlockPos().relative(facing);
            BlockState nextBlock = level.getBlockState(nextPos);
            if (nextBlock.is(ModBlocks.CONVEYOR_BELT.get())) {
                // Item continues on next belt
            }
        }
    }

    /**
     * Gets movement direction of this conveyor belt.
     *
     * @return Movement direction
     */
    public Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(ConveyorBeltBlock.FACING)) {
            return state.getValue(ConveyorBeltBlock.FACING);
        }
        return Direction.NORTH;
    }
}
