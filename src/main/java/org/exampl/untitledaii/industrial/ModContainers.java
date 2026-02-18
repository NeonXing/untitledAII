package org.exampl.untitledaii.industrial;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.exampl.untitledaii.Untitledaii;
import org.exampl.untitledaii.industrial.machine.CrusherContainer;

/**
 * Container registry for industrial mod.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class ModContainers {

    public static final DeferredRegister<MenuType<?>> CONTAINERS = 
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, Untitledaii.MODID);

    public static final RegistryObject<MenuType<CrusherContainer>> CRUSHER = 
        CONTAINERS.register("crusher", () -> CrusherContainer::new);
}
