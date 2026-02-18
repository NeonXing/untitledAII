package org.exampl.untitledaii.industrial;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.exampl.untitledaii.Untitledaii;
import org.exampl.untitledaii.industrial.energy.EnergyCableBlock;

/**
 * Block registry for industrial mod.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(ForgeRegistries.BLOCKS, Untitledaii.MODID);

    public static final RegistryObject<Block> ENERGY_CABLE = BLOCKS.register("energy_cable",
        () -> new EnergyCableBlock());
}
