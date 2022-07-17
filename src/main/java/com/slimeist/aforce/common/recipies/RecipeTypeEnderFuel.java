package com.slimeist.aforce.common.recipies;

import com.slimeist.aforce.AdvancedForcefields;
import net.minecraft.world.item.crafting.RecipeType;

public class RecipeTypeEnderFuel implements RecipeType<EnderFuelRecipe> {

    @Override
    public String toString() {
        return AdvancedForcefields.getId("ender_fuel_recipe").toString();
    }
}
