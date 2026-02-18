package org.exampl.untitledaii.industrial;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.exampl.untitledaii.Untitledaii;
import org.exampl.untitledaii.industrial.conveyor.ConveyorBeltBlockEntity;
import org.exampl.untitledaii.industrial.energy.EnergyCableBlockEntity;
import org.exampl.untitledaii.industrial.machine.CrusherBlockEntity;
import org.exampl.untitledaii.industrial.pipe.ItemPipeBlockEntity;

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

    public static final RegistryObject<BlockEntityType<CrusherBlockEntity>> CRUSHER = 
        BLOCK_ENTITIES.register("crusher",
            () -> BlockEntityType.Builder.of(
                CrusherBlockEntity::new,
                ModBlocks.CRUSHER.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ConveyorBeltBlockEntity>> CONVEYOR_BELT = 
        BLOCK_ENTITIES.register("conveyor_belt",
            () -> BlockEntityType.Builder.of(
                ConveyorBeltBlockEntity::new,
                ModBlocks.CONVEYOR_BELT.get()
            ).build(null));

    public static final RegistryObject<BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE = 
        BLOCK_ENTITIES.register("item_pipe",
            () -> BlockEntityType.Builder.of(
                ItemPipeBlockEntity::new,
                ModBlocks.ITEM_PIPE.get()
            ).build(null));
}
