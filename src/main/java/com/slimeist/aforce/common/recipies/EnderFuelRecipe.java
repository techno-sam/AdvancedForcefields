package com.slimeist.aforce.common.recipies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.StartupCommon;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class EnderFuelRecipe implements IRecipe<IInventory> {

    public static final Serializer SERIALIZER = new Serializer();

    private final Ingredient input;
    private final int fuelTicks;
    private final ResourceLocation id;

    public EnderFuelRecipe(ResourceLocation id, Ingredient input, int fuelTicks) {
        this.id = id;
        this.input = input;
        this.fuelTicks = fuelTicks;

        //AdvancedForcefields.LOGGER.info("Loaded recipe: " + this.toString());
    }

    @Override
    public String toString () {

        // Overriding toString is not required, it's just useful for debugging.
        return "EnderFuelRecipe [input=" + this.input + ", fuelTicks=" + this.fuelTicks + ", id=" + this.id + "]";
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {

        // This method is ignored by our custom recipe system, and only has partial
        // functionality. isValid is used instead.
        return this.input.test(inv.getItem(36));
    }

    public boolean matches(ItemStack stack, World world) {
        return this.input.test(stack);
    }

    public int getFuelTicks() {
        return this.fuelTicks;
    }

    @Override
    public ItemStack assemble(IInventory p_77572_1_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int x, int y) {
        return (x>0) && (y>0);
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(IInventory p_179532_1_) {
        return IRecipe.super.getRemainingItems(p_179532_1_);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return IRecipe.super.getIngredients();
    }

    @Override
    public boolean isSpecial() {
        return IRecipe.super.isSpecial();
    }

    @Override
    public String getGroup() {
        return IRecipe.super.getGroup();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public IRecipeType<?> getType() {
        return StartupCommon.ENDER_FUEL_RECIPE;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Items.ENDER_PEARL);
    }

    private static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<EnderFuelRecipe> {

        Serializer() {
            this.setRegistryName(AdvancedForcefields.getId("ender_fuel_recipe"));
        }

        @Override
        public EnderFuelRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            final JsonElement inputElement = JSONUtils.isArrayNode(json, "input") ? JSONUtils.getAsJsonArray(json, "input") : JSONUtils.getAsJsonObject(json, "input");
            final Ingredient input = Ingredient.fromJson(inputElement);

            final int fuelTicks = JSONUtils.getAsInt(json, "fuelTicks");

            return new EnderFuelRecipe(recipeId, input, fuelTicks);
        }

        @Override
        public EnderFuelRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            final Ingredient input = Ingredient.fromNetwork(buffer);

            final int fuelTicks = buffer.readInt();

            return new EnderFuelRecipe(recipeId, input, fuelTicks);
        }

        @Override
        public void toNetwork(PacketBuffer buffer, EnderFuelRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeInt(recipe.fuelTicks);
        }
    }
}
