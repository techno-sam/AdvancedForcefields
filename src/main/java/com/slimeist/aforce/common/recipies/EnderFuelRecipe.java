package com.slimeist.aforce.common.recipies;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.StartupCommon;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class EnderFuelRecipe implements Recipe<Container> {

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
    public boolean matches(Container inv, Level worldIn) {

        // This method is ignored by our custom recipe system, and only has partial
        // functionality. isValid is used instead.
        return this.input.test(inv.getItem(36));
    }

    public boolean matches(ItemStack stack, Level world) {
        return this.input.test(stack);
    }

    public int getFuelTicks() {
        return this.fuelTicks;
    }

    @Override
    public ItemStack assemble(Container p_77572_1_) {
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
    public NonNullList<ItemStack> getRemainingItems(Container p_179532_1_) {
        return Recipe.super.getRemainingItems(p_179532_1_);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return Recipe.super.getIngredients();
    }

    @Override
    public boolean isSpecial() {
        return Recipe.super.isSpecial();
    }

    @Override
    public String getGroup() {
        return Recipe.super.getGroup();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return StartupCommon.ENDER_FUEL_RECIPE;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Items.ENDER_PEARL);
    }

    private static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<EnderFuelRecipe> {

        Serializer() {
            this.setRegistryName(AdvancedForcefields.getId("ender_fuel_recipe"));
        }

        @Override
        public EnderFuelRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            final JsonElement inputElement = GsonHelper.isArrayNode(json, "input") ? GsonHelper.getAsJsonArray(json, "input") : GsonHelper.getAsJsonObject(json, "input");
            final Ingredient input = Ingredient.fromJson(inputElement);

            final int fuelTicks = GsonHelper.getAsInt(json, "fuelTicks");

            return new EnderFuelRecipe(recipeId, input, fuelTicks);
        }

        @Override
        public EnderFuelRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            final Ingredient input = Ingredient.fromNetwork(buffer);

            final int fuelTicks = buffer.readInt();

            return new EnderFuelRecipe(recipeId, input, fuelTicks);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, EnderFuelRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeInt(recipe.fuelTicks);
        }
    }
}
