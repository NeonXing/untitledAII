package org.exampl.untitledaii.industrial;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.exampl.untitledaii.Untitledaii;
import org.exampl.untitledaii.industrial.energy.EnergyCableBlockEntity;

/**
 * Block entity registry for industrial mod.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Untitledaii.MODID);

    public static final RegistryObject<BlockEntityType<EnergyCableBlockEntity>> ENERGY_CABLE = 
        BLOCK_ENTITIES.register("energy_cable",
            () -> BlockEntityType.Builder.of(
                EnergyCableBlockEntity::new,
                ModBlocks.ENERGY_CABLE.get()
            ).build(null));
}
