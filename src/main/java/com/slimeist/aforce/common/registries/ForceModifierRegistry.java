package com.slimeist.aforce.common.registries;

import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class ForceModifierRegistry extends ForgeRegistryEntry<ForceModifierRegistry> {
    protected final Item trigger;
    protected final IForceModifierAction action;

    public ForceModifierRegistry(Item trigger, IForceModifierAction action) {
        this.trigger = trigger;
        this.action = action;
    }

    public Item getTrigger() {
        return this.trigger;
    }

    public IForceModifierAction getAction() {
        return this.action;
    }
}
