package com.slimeist.aforce.common.registries;

import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Supplier;

public class ForceModifierRegistry extends ForgeRegistryEntry<ForceModifierRegistry> {
    protected final Supplier<Ingredient> trigger;
    protected final IForceModifierAction action;

    public ForceModifierRegistry(ITag<Item> trigger, IForceModifierAction action) {
        this(() -> Ingredient.of(trigger), action);
    }

    public ForceModifierRegistry(Item trigger, IForceModifierAction action) {
        this(() -> Ingredient.of(trigger), action);
    }

    public ForceModifierRegistry(Ingredient trigger, IForceModifierAction action) {
        this(() -> trigger, action);
    }

    public ForceModifierRegistry(Supplier<Ingredient> trigger, IForceModifierAction action) {
        this.trigger = trigger;
        this.action = action;
    }

    public boolean matches(ItemStack test) {
        return this.trigger.get().test(test);
    }

    public IForceModifierAction getAction() {
        return this.action;
    }
}
