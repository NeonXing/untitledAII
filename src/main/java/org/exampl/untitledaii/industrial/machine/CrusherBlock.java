package org.exampl.untitledaii.industrial.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.exampl.untitledaii.industrial.ModBlocks;

/**
 * Crusher block for crushing ores into ingots.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class CrusherBlock extends Block {

    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    public CrusherBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(3.5f, 4.0f)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false));
        this.registerDefaultState();
    }

    private void registerDefaultState() {
        this.defaultBlockState()
            .setValue(FACING, net.minecraft.core.Direction.NORTH);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrusherBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.box(0, 0, 0, 16, 16, 16);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, LevelReader level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CrusherBlockEntity) {
            CrusherBlockEntity crusher = (CrusherBlockEntity) be;
            return crusher.isProcessing() ? 15 : 0;
        }
        return 0;
    }
}
