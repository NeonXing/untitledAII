package org.exampl.untitledaii.industrial.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Item pipe block for transporting items.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class ItemPipeBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    private static final VoxelShape CORE = Shapes.box(6, 6, 6, 10, 10, 10);
    private static final VoxelShape NORTH_ARM = Shapes.box(6, 6, 0, 10, 10, 6);
    private static final VoxelShape SOUTH_ARM = Shapes.box(6, 6, 10, 10, 10, 16);
    private static final VoxelShape EAST_ARM = Shapes.box(10, 6, 6, 16, 10, 10);
    private static final VoxelShape WEST_ARM = Shapes.box(0, 6, 6, 6, 10, 10);
    private static final VoxelShape UP_ARM = Shapes.box(6, 10, 6, 10, 16, 10);
    private static final VoxelShape DOWN_ARM = Shapes.box(6, 0, 6, 10, 6, 10);
    private static final VoxelShape FULL_SHAPE = Shapes.or(
        CORE, NORTH_ARM, SOUTH_ARM, EAST_ARM, WEST_ARM, UP_ARM, DOWN_ARM
    );

    public ItemPipeBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(0.5f, 0.5f)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemPipeBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_SHAPE;
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
            if (blockEntity instanceof ItemPipeBlockEntity) {
                ((ItemPipeBlockEntity) blockEntity).invalidateCaps();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
