package com.slimeist.aforce.common.recipies;

import com.slimeist.aforce.AdvancedForcefields;
import net.minecraft.item.crafting.IRecipeType;

public class RecipeTypeEnderFuel implements IRecipeType<EnderFuelRecipe> {

    @Override
    public String toString() {
        return AdvancedForcefields.getId("ender_fuel_recipe").toString();
    }
}
