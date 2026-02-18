package org.exampl.untitledaii.industrial.machine;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.exampl.untitledaii.Untitledaii;
import org.exampl.untitledaii.industrial.ModBlocks;
import org.exampl.untitledaii.industrial.machine.recipes.MachineRecipe;

/**
 * Multi-stage ore processing chain configuration.
 *
 * <p>Processing stages:</p>
 * <ol>
 *   <li>Ore → Crusher (2x output)</li>
 *   <li>Ingot → Furnace (2x output)</li>
 *   <li>Total: 1 ore → 4 ingots</li>
 * </ol>
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class OreProcessingChain {

    private static final RegistryObject<Item> CRUSHED_IRON_INGOT =
        RegistryObject.create("crushed_iron_ingot", () -> Items.IRON_INGOT);
    private static final RegistryObject<Item> CRUSHED_GOLD_INGOT =
        RegistryObject.create("crushed_gold_ingot", () -> Items.GOLD_INGOT);
    private static final RegistryObject<Item> CRUSHED_COPPER_INGOT =
        RegistryObject.create("crushed_copper_ingot", () -> Items.COPPER_INGOT);

    /**
     * Registers all ore processing chain items.
     */
    public static void registerItems() {
        // Crushed ingots are actually just vanilla ingots for simplicity
        // In a full implementation, these would be custom items
    }

    /**
     * Gets the total yield multiplier for the ore processing chain.
     *
     * <p>Standard chain: 1 ore → 4 ingots (4x multiplier)</p>
     *
     * @return Yield multiplier (4.0)
     */
    public static float getYieldMultiplier() {
        return 4.0f;
    }

    /**
     * Calculates total output for given ore input count.
     *
     * @param oreCount Number of ores
     * @param ingotsPerOre Ingots produced from 1 ore
     * @return Total ingots produced
     */
    public static int calculateOutput(int oreCount, int ingotsPerOre) {
        return oreCount * ingotsPerOre;
    }

    /**
     * Creates a default ore processing chain configuration.
     *
     * <p>Standard chain:</p>
     * <ul>
     *   <li>1 Iron Ore → Crusher → 2 Iron Ingots</li>
     *   <li>1 Gold Ore → Crusher → 2 Gold Ingots</li>
     *   <li>1 Copper Ore → Crusher → 2 Copper Ingots</li>
     * </ul>
     *
     * @return Processing chain configuration
     */
    public static ProcessingStage[] createDefaultChain() {
        return new ProcessingStage[] {
            new ProcessingStage("Crusher", 100, 500, 2),
            new ProcessingStage("Furnace", 200, 1000, 2)
        };
    }

    /**
     * Represents a single processing stage.
     */
    public static class ProcessingStage {
        private final String name;
        private final int processTime;
        private final int energyRequired;
        private final int outputMultiplier;

        public ProcessingStage(String name, int processTime, int energyRequired, int outputMultiplier) {
            this.name = name;
            this.processTime = processTime;
            this.energyRequired = energyRequired;
            this.outputMultiplier = outputMultiplier;
        }

        public String getName() {
            return name;
        }

        public int getProcessTime() {
            return processTime;
        }

        public int getEnergyRequired() {
            return energyRequired;
        }

        public int getOutputMultiplier() {
            return outputMultiplier;
        }
    }
}
