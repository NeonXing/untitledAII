package org.exampl.untitledaii.industrial.machine.recipes;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.exampl.untitledaii.Untitledaii;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Recipe type for machine recipes.
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class MachineRecipeType implements RecipeType<MachineRecipe>, RecipeSerializer<MachineRecipe> {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String ID = "machine";
    public static final ResourceLocation TYPE_ID =
        ResourceLocation.fromNamespaceAndPath(Untitledaii.MODID, ID);
    public static final ResourceLocation SERIALIZER_ID =
        ResourceLocation.fromNamespaceAndPath(Untitledaii.MODID, "serializer");

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = 
        DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Untitledaii.MODID);
    
    public static final RegistryObject<RecipeType<MachineRecipe>> RECIPE_TYPE = 
        RECIPE_TYPES.register(ID, () -> new MachineRecipeType());

    @Override
    public String toString() {
        return ID;
    }

    @Nullable
    @Override
    public MachineRecipe fromJson(ResourceLocation id, JsonObject json) {
        try {
            // Parse inputs
            var inputsArray = json.getAsJsonArray("inputs");
            NonNullList<Ingredient> inputs = NonNullList.withSize(inputsArray.size(), Ingredient.EMPTY);
            for (int i = 0; i < inputsArray.size(); i++) {
                inputs.set(i, Ingredient.fromJson(inputsArray.get(i)));
            }

            // Parse outputs
            var outputsArray = json.getAsJsonArray("outputs");
            NonNullList<ItemStack> outputs = NonNullList.withSize(outputsArray.size(), ItemStack.EMPTY);
            for (int i = 0; i < outputsArray.size(); i++) {
                JsonObject outputObj = outputsArray.get(i).getAsJsonObject();
                String itemId = outputObj.get("item").getAsString();
                int count = outputObj.has("count") ? outputObj.get("count").getAsInt() : 1;
                outputs.set(i, new ItemStack(net.minecraftforge.registries.ForgeRegistries.ITEMS
                    .getValue(ResourceLocation.parse(itemId)), count));
            }

            // Parse process time
            int processTime = json.has("process_time")
                ? json.get("process_time").getAsInt()
                : 100;

            // Parse energy required
            int energyRequired = json.has("energy_required")
                ? json.get("energy_required").getAsInt()
                : 500;

            return new MachineRecipe(id, inputs, outputs, processTime, energyRequired);

        } catch (Exception e) {
            LOGGER.error("Failed to load machine recipe {}: {}", id, e.getMessage());
            return null;
        }
    }

    @Nullable
    @Override
    public MachineRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return MachineRecipe.fromNetwork(buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, MachineRecipe recipe) {
        recipe.toNetwork(buffer);
    }

    /**
     * Finds a recipe for given input items in the given level.
     *
     * @param level The level to search in
     * @param input The input item stack
     * @return Matching recipe or null
     */
    @Nullable
    public static MachineRecipe findRecipe(Level level, ItemStack input) {
        RecipeManager recipeManager = level.getRecipeManager();
        RecipeType<MachineRecipe> type = (RecipeType<MachineRecipe>) RECIPE_TYPE.get();
        return recipeManager.getAllRecipesFor(type).stream()
            .filter(recipe -> recipe.matches(input))
            .findFirst()
            .orElse(null);
    }

    /**
     * Finds a recipe for given input items in the given level.
     *
     * @param level The level to search in
     * @param inputs The input item stacks
     * @return Matching recipe or null
     */
    @Nullable
    public static MachineRecipe findRecipe(Level level, NonNullList<ItemStack> inputs) {
        RecipeManager recipeManager = level.getRecipeManager();
        RecipeType<MachineRecipe> type = (RecipeType<MachineRecipe>) RECIPE_TYPE.get();
        return recipeManager.getAllRecipesFor(type).stream()
            .filter(recipe -> recipe.matches(inputs))
            .findFirst()
            .orElse(null);
    }
}
