package org.exampl.untitledaii.industrial;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.exampl.untitledaii.Untitledaii;
import org.exampl.untitledaii.industrial.machine.upgrades.MachineUpgrade;

/**
 * Item registry for industrial mod.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, Untitledaii.MODID);

    public static final RegistryObject<Item> ENERGY_CABLE = ITEMS.register("energy_cable",
        () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CRUSHER = ITEMS.register("crusher",
        () -> new Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CONVEYOR_BELT = ITEMS.register("conveyor_belt",
        () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> ITEM_PIPE = ITEMS.register("item_pipe",
        () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SPEED_UPGRADE = ITEMS.register("speed_upgrade",
        () -> new MachineUpgrade(new Item.Properties(), MachineUpgrade.UpgradeType.SPEED_UPGRADE));

    public static final RegistryObject<Item> ENERGY_UPGRADE = ITEMS.register("energy_upgrade",
        () -> new MachineUpgrade(new Item.Properties(), MachineUpgrade.UpgradeType.ENERGY_UPGRADE));

    public static final RegistryObject<Item> CRUSHED_IRON = ITEMS.register("crushed_iron",
        () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CRUSHED_GOLD = ITEMS.register("crushed_gold",
        () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CRUSHED_COPPER = ITEMS.register("crushed_copper",
        () -> new Item(new Item.Properties()));
}
