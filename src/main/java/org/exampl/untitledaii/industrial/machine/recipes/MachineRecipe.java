package org.exampl.untitledaii.industrial.machine.recipes;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.ICondition;

/**
 * Base class for machine recipes.
 *
 * <p>Recipe structure:</p>
 * <ul>
 *   <li>Input ingredients</li>
 *   <li>Output items</li>
 *   <li>Process time (ticks)</li>
 *   <li>Energy required (FE)</li>
 * </ul>
 *
 * @author AVA Industrial Team
 * @since 1.0.0
 */
public class MachineRecipe {

    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final NonNullList<ItemStack> outputs;
    private final int processTime;
    private final int energyRequired;

    public MachineRecipe(ResourceLocation id, NonNullList<Ingredient> inputs,
                     NonNullList<ItemStack> outputs, int processTime, int energyRequired) {
        this.id = id;
        this.inputs = inputs;
        this.outputs = outputs;
        this.processTime = processTime;
        this.energyRequired = energyRequired;
    }

    /**
     * Checks if recipe matches given input stacks.
     *
     * @param stacks Input item stacks
     * @return true if recipe matches
     */
    public boolean matches(NonNullList<ItemStack> stacks) {
        if (stacks.size() != inputs.size()) {
            return false;
        }

        for (int i = 0; i < inputs.size(); i++) {
            if (i >= stacks.size() || !inputs.get(i).test(stacks.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if recipe matches given ItemStack.
     *
     * @param stack Input item stack
     * @return true if recipe matches
     */
    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && !inputs.isEmpty() && inputs.get(0).test(stack);
    }

    public ResourceLocation getId() {
        return id;
    }

    public NonNullList<Ingredient> getInputs() {
        return inputs;
    }

    public NonNullList<ItemStack> getOutputs() {
        return outputs;
    }

    public int getProcessTime() {
        return processTime;
    }

    public int getEnergyRequired() {
        return energyRequired;
    }

    /**
     * Writes recipe to network buffer.
     */
    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(id);
        buffer.writeInt(inputs.size());
        inputs.forEach(ingredient -> Ingredient.toNetwork(ingredient, buffer));
        buffer.writeInt(outputs.size());
        outputs.forEach(stack -> buffer.writeItem(stack));
        buffer.writeInt(processTime);
        buffer.writeInt(energyRequired);
    }

    /**
     * Reads recipe from network buffer.
     */
    public static MachineRecipe fromNetwork(FriendlyByteBuf buffer) {
        ResourceLocation id = buffer.readResourceLocation();
        int inputCount = buffer.readInt();
        NonNullList<Ingredient> inputs = NonNullList.withSize(inputCount, Ingredient.EMPTY);
        for (int i = 0; i < inputCount; i++) {
            inputs.set(i, Ingredient.fromNetwork(buffer));
        }
        int outputCount = buffer.readInt();
        NonNullList<ItemStack> outputs = NonNullList.withSize(outputCount, ItemStack.EMPTY);
        for (int i = 0; i < outputCount; i++) {
            outputs.set(i, buffer.readItem());
        }
        int processTime = buffer.readInt();
        int energyRequired = buffer.readInt();
        return new MachineRecipe(id, inputs, outputs, processTime, energyRequired);
    }
}
