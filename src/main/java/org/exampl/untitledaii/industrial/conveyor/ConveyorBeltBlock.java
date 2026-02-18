package org.exampl.untitledaii.industrial.conveyor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Conveyor belt block for transporting items.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class ConveyorBeltBlock extends Block {

    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    private static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 16, 2, 16);

    public ConveyorBeltBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(0.5f, 0.5f)
            .noOcclusion()
            .noCollission()
            .isValidSpawn((state, level, pos, type) -> false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConveyorBeltBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            level.updateNeighborsAt(pos, this);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ConveyorBeltBlockEntity) {
                ((ConveyorBeltBlockEntity) blockEntity).invalidateCaps();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
