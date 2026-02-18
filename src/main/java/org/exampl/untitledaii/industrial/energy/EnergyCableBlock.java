package org.exampl.untitledaii.industrial.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Energy cable block for transmitting Forge Energy (FE).
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Transfers energy between machines</li>
 *   <li>Supports multiple cables connecting</li>
 *   <li>Visual connection rendering</li>
 * </ul>
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class EnergyCableBlock extends Block implements EntityBlock {

    public static final IntegerProperty ENERGY_LEVEL = BlockStateProperties.POWER;

    public EnergyCableBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(3.0f, 6.0f)
            .noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENERGY_LEVEL);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCableBlockEntity(pos, state);
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
            if (blockEntity instanceof EnergyCableBlockEntity) {
                ((EnergyCableBlockEntity) blockEntity).invalidateCaps();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
