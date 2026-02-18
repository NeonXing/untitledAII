package org.exampl.untitledaii;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.exampl.untitledaii.industrial.ModBlockEntities;
import org.exampl.untitledaii.industrial.ModBlocks;
import org.slf4j.Logger;

/**
 * AVA Industrial Mod - Main class
 *
 * <p>An industrial automation mod for Minecraft featuring:</p>
 * <ul>
 *   <li>Energy system using Forge Energy API (FE)</li>
 *   <li>Multi-stage ore processing</li>
 *   <li>Automated machine systems</li>
 *   <li>Conveyor belt automation</li>
 *   <li>Pipe networks for energy, items, and fluids</li>
 * </ul>
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
@Mod(Untitledaii.MODID)
public class Untitledaii {

    public static final String MODID = "untitledaii";
    private static final Logger LOGGER = LogUtils.getLogger();

    // Deferred Registers
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Example creative tab
    public static final RegistryObject<CreativeModeTab> INDUSTRIAL_TAB = CREATIVE_MODE_TABS.register("industrial_tab",
        () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(net.minecraft.network.chat.Component.translatable("itemGroup.untitledaii.industrial"))
            .displayItems((parameters, output) -> {
                // Industrial items will be added here
            })
            .build());

    public Untitledaii() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        
        // Register industrial mod components
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModContainers.CONTAINERS.register(modEventBus);
        org.exampl.untitledaii.industrial.machine.recipes.MachineRecipeType.RECIPE_TYPE.register(modEventBus);
        org.exampl.untitledaii.industrial.machine.recipes.MachineRecipeType.SERIALIZER.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("AVA Industrial Mod initializing...");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("AVA Industrial Mod loaded successfully!");
        LOGGER.info("Energy system: Forge Energy API (FE)");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("AVA Industrial: Server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("AVA Industrial: Client setup complete");
        }
    }
}
