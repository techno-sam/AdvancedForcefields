package com.slimeist.aforce.common.tiles.helpers;

import com.slimeist.aforce.core.util.MiscUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class AdvancedForceModifierSelector extends BaseForceModifierSelector {

    public static String TAG_WHITELIST = "whitelist";
    protected boolean whitelist = false;

    public static String TAG_ENTITY_SELECTOR = "entitySelector";
    protected String entitySelector = "@e";

    private Predicate<Entity> entityPredicate;

    public AdvancedForceModifierSelector() {
        super();
    }

    public AdvancedForceModifierSelector(BlockPos originPosition) {
        super(originPosition);
    }

    public AdvancedForceModifierSelector(boolean whitelist, String entitySelector, String action, int priority, BlockPos originPosition, ItemStack triggerStack) {
        super(action, priority, originPosition, triggerStack);
        this.whitelist = whitelist;
        this.entitySelector = entitySelector;
        this.updatePredicate();
    }

    protected void updatePredicate() {
        this.entityPredicate = MiscUtil.predicateFromSelector(this.entitySelector).orElse(null);
    }

    @Override
    public ForceModifierSelectorType getType() {
        return ForceModifierSelectorType.ADVANCED;
    }

    @Override
    protected void loadNBT(CompoundNBT nbt) {
        super.loadNBT(nbt);

        if (nbt.contains(TAG_WHITELIST, Constants.NBT.TAG_BYTE)) {
            this.setWhitelist(nbt.getBoolean(TAG_WHITELIST));
        }

        if (nbt.contains(TAG_ENTITY_SELECTOR, Constants.NBT.TAG_STRING)) {
            this.setEntitySelector(nbt.getString(TAG_ENTITY_SELECTOR));
        }
    }

    @Override
    public CompoundNBT toNBT() {
        CompoundNBT nbt = super.toNBT();

        nbt.putBoolean(TAG_WHITELIST, this.isWhitelist());

        nbt.putString(TAG_ENTITY_SELECTOR, this.getEntitySelector());

        return nbt;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public String getEntitySelector() {
        return this.entitySelector;
    }

    public void setEntitySelector(String entitySelector) {
        this.entitySelector = entitySelector;
        this.updatePredicate();
    }

    public static void info(String msg) {
        //AdvancedForcefields.LOGGER.info(msg);
    }

    public static String listToString(List<String> list) {
        if (list.size()<=0) {
            return "{}";
        }
        StringBuilder ret = new StringBuilder("[");

        for (String s : list) {
            ret.append(s).append(", ");
        }

        ret = new StringBuilder(ret.substring(0, ret.length() - 2));
        ret.append("]");

        return ret.toString();
    }

    protected boolean predicateTest(@Nonnull Entity entity) {
        return entityPredicate != null && entityPredicate.test(entity);
    }

    @Override
    public boolean validForEntity(@Nonnull Entity entity) {
        info("Checking for entity: "+entity.getName().getString());
        if (entity.level == null) {
            info("Entity does not exist or it has no level");
            return false;
        }

        if (whitelist^predicateTest(entity)) {
            info("Whitelist state: "+(whitelist ? "on" : "off")+", TargetList of: "+this.getEntitySelector()+", contains name: "+(this.getEntitySelector().equals(entity.getName().getString()) ? "yes" : "no"));
            return false;
        }

        return true;
    }
}
