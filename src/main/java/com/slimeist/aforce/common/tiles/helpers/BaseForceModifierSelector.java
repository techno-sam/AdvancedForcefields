package com.slimeist.aforce.common.tiles.helpers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.core.util.TagUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.AbstractMap;
import java.util.function.Supplier;

public class BaseForceModifierSelector {

    public static String TAG_SELECTOR_TYPE = "selectorType";

    public static String TAG_MODIFIER_ACTION = "modifierAction";
    protected String action = AdvancedForcefields.getId("default_action").toString();

    public static String TAG_PRIORITY = "priority";
    protected int priority = 0;

    public static String TAG_ORIGIN_POSITION = "originPosition";
    protected BlockPos originPosition = null;

    public static String TAG_TRIGGER_STACK = "triggerStack";
    protected ItemStack triggerStack = ItemStack.EMPTY;

    public BaseForceModifierSelector() {
        ;
    }

    public BaseForceModifierSelector(BlockPos originPosition) {
        this.originPosition = originPosition;
    }

    public BaseForceModifierSelector(String action, int priority, BlockPos originPosition, ItemStack triggerStack) {
        this(originPosition);
        this.action = action;
        this.priority = priority;
        this.triggerStack = triggerStack;
    }

    public ForceModifierSelectorType getType() {
        return ForceModifierSelectorType.BASE;
    }

    public static BaseForceModifierSelector fromNBT(CompoundNBT nbt) {
        String type = "";
        if (nbt.contains(TAG_SELECTOR_TYPE, Constants.NBT.TAG_STRING)) {
            type = nbt.getString(TAG_SELECTOR_TYPE);
        }
        BaseForceModifierSelector instance;
        try {
            instance = ForceModifierSelectorType.valueOf(type).create();
        } catch (IllegalArgumentException ignored) {
            instance = new BaseForceModifierSelector();
        }
        instance.loadNBT(nbt);
        return instance;
    }

    protected void loadNBT(CompoundNBT nbt) {
        this.originPosition = TagUtil.readPos(nbt.getCompound(TAG_ORIGIN_POSITION));

        if (nbt.contains(TAG_MODIFIER_ACTION, Constants.NBT.TAG_STRING)) {
            this.setAction(nbt.getString(TAG_MODIFIER_ACTION));
        }

        if (nbt.contains(TAG_PRIORITY, Constants.NBT.TAG_INT)) {
            this.setPriority(nbt.getInt(TAG_PRIORITY));
        }

        if (nbt.contains(TAG_TRIGGER_STACK, Constants.NBT.TAG_COMPOUND)) {
            this.setTriggerStack(ItemStack.of(nbt.getCompound(TAG_TRIGGER_STACK)));
        }
    }

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putString(TAG_SELECTOR_TYPE, this.getType().name());

        nbt.putString(TAG_MODIFIER_ACTION, this.getAction());
        nbt.putInt(TAG_PRIORITY, this.getPriority());

        nbt.put(TAG_ORIGIN_POSITION, TagUtil.writePos(this.getOriginPosition()));

        CompoundNBT itemNBT = new CompoundNBT();

        this.getTriggerStack().save(itemNBT);

        nbt.put(TAG_TRIGGER_STACK, itemNBT);

        return nbt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public BlockPos getOriginPosition() {
        return originPosition;
    }

    public void setOriginPosition(BlockPos originPosition) {
        this.originPosition = originPosition;
    }

    public ItemStack getTriggerStack() {
        return triggerStack;
    }

    public void setTriggerStack(ItemStack triggerStack) {
        this.triggerStack = triggerStack;
    }

    public boolean validForEntity(@Nonnull Entity entity) {
        return true;
    }
}
