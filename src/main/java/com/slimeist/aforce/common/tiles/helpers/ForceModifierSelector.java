package com.slimeist.aforce.common.tiles.helpers;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.core.util.TagUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ForceModifierSelector {

    public static String TAG_TARGET_LIST = "targetList";
    protected List<String> targetList = new ArrayList<>();

    public static String TAG_WHITELIST = "whitelist";
    protected boolean whitelist = false;


    public static String TAG_TARGET_ANIMALS = "targetAnimals";
    protected boolean targetAnimals = false;

    public static String TAG_TARGET_PLAYERS = "targetPlayers";
    protected boolean targetPlayers = false;

    public static String TAG_TARGET_NEUTRALS = "targetNeutrals";
    protected boolean targetNeutrals = false;

    public static String TAG_MODIFIER_ACTION = "modifierAction";
    protected String action = AdvancedForcefields.getId("default_action").toString();

    public static String TAG_PRIORITY = "priority";
    protected int priority = 0;

    public static String TAG_ORIGIN_POSITION = "originPosition";
    protected BlockPos originPosition = null;

    public static String TAG_TRIGGER_STACK = "triggerStack";
    protected ItemStack triggerStack = ItemStack.EMPTY;

    public ForceModifierSelector(BlockPos originPosition) {
        this.originPosition = originPosition;
    }

    public ForceModifierSelector(List<String> targetList, boolean whitelist, boolean targetAnimals, boolean targetPlayers, boolean targetNeutrals, String action, int priority, BlockPos originPosition, ItemStack triggerStack) {
        this.targetList = targetList;
        this.whitelist = whitelist;
        this.targetAnimals = targetAnimals;
        this.targetPlayers = targetPlayers;
        this.targetNeutrals = targetNeutrals;
        this.action = action;
        this.priority = priority;
        this.originPosition = originPosition;
        this.triggerStack = triggerStack;
    }

    public static ForceModifierSelector fromNBT(CompoundTag nbt) {
        ForceModifierSelector selector = new ForceModifierSelector(TagUtil.readPos(nbt.getCompound(TAG_ORIGIN_POSITION)));

        if (nbt.contains(TAG_TARGET_LIST, Tag.TAG_LIST)) {
            ListTag list = nbt.getList(TAG_TARGET_LIST, Tag.TAG_STRING);
            List<String> tlist = new ArrayList<>();
            for (int i=0; i<list.size(); i++) {
                tlist.add(list.getString(i));
            }
            selector.setTargetList(tlist);
        }

        if (nbt.contains(TAG_WHITELIST, Tag.TAG_BYTE)) {
            selector.setWhitelist(nbt.getBoolean(TAG_WHITELIST));
        }

        if (nbt.contains(TAG_TARGET_ANIMALS, Tag.TAG_BYTE)) {
            selector.setTargetAnimals(nbt.getBoolean(TAG_TARGET_ANIMALS));
        }

        if (nbt.contains(TAG_TARGET_PLAYERS, Tag.TAG_BYTE)) {
            selector.setTargetPlayers(nbt.getBoolean(TAG_TARGET_PLAYERS));
        }

        if (nbt.contains(TAG_TARGET_NEUTRALS, Tag.TAG_BYTE)) {
            selector.setTargetNeutrals(nbt.getBoolean(TAG_TARGET_NEUTRALS));
        }

        if (nbt.contains(TAG_MODIFIER_ACTION, Tag.TAG_STRING)) {
            selector.setAction(nbt.getString(TAG_MODIFIER_ACTION));
        }

        if (nbt.contains(TAG_PRIORITY, Tag.TAG_INT)) {
            selector.setPriority(nbt.getInt(TAG_PRIORITY));
        }

        if (nbt.contains(TAG_TRIGGER_STACK, Tag.TAG_COMPOUND)) {
            selector.setTriggerStack(ItemStack.of(nbt.getCompound(TAG_TRIGGER_STACK)));
        }

        return selector;
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();

        ListTag tlist = new ListTag();
        for (String target : this.getTargetList()) {
            tlist.add(StringTag.valueOf(target));
        }
        nbt.put(TAG_TARGET_LIST, tlist);

        nbt.putBoolean(TAG_WHITELIST, this.isWhitelist());

        nbt.putBoolean(TAG_TARGET_ANIMALS, this.shouldTargetAnimals());
        nbt.putBoolean(TAG_TARGET_PLAYERS, this.shouldTargetPlayers());
        nbt.putBoolean(TAG_TARGET_NEUTRALS, this.shouldTargetNeutrals());

        nbt.putString(TAG_MODIFIER_ACTION, this.getAction());
        nbt.putInt(TAG_PRIORITY, this.getPriority());

        nbt.put(TAG_ORIGIN_POSITION, TagUtil.writePos(this.getOriginPosition()));

        CompoundTag itemNBT = new CompoundTag();

        this.getTriggerStack().save(itemNBT);

        nbt.put(TAG_TRIGGER_STACK, itemNBT);

        return nbt;
    }

    public List<String> getTargetList() {
        return targetList;
    }

    public void setTargetList(List<String> targetList) {
        this.targetList = targetList;
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    public boolean shouldTargetAnimals() {
        return targetAnimals;
    }

    public void setTargetAnimals(boolean targetAnimals) {
        this.targetAnimals = targetAnimals;
    }

    public boolean shouldTargetPlayers() {
        return targetPlayers;
    }

    public void setTargetPlayers(boolean targetPlayers) {
        this.targetPlayers = targetPlayers;
    }

    public boolean shouldTargetNeutrals() {
        return targetNeutrals;
    }

    public void setTargetNeutrals(boolean targetNeutrals) {
        this.targetNeutrals = targetNeutrals;
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

    //the following method, validForEntity is from ImmersiveEngineering turret validation check, credit to BluSunrize
    public boolean validForEntity(@Nonnull Entity entity) {
        info("Checking for entity: "+entity.getName().getString());
        if (entity.level == null) {
            info("Entity does not exist or it has no level");
            return false;
        }

        if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth()<=0) {
            info("Entity is dead");
            return false;
        }

        if (whitelist^this.getTargetList().contains(entity.getName().getString())) {
            info("Whitelist state: "+(whitelist ? "on" : "off")+", TargetList of: "+listToString(this.getTargetList())+", contains name: "+(this.getTargetList().contains(entity.getName().getString()) ? "yes" : "no"));
            return false;
        }

        if (entity instanceof Animal &&!this.shouldTargetAnimals()) {
            info("Entity is an animal, and we don't target them.");
            return false;
        }
        if (entity instanceof Player &&!this.shouldTargetPlayers()) {
            info("Entity is a player, and we don't target them.");
            return false;
        }
        if (!(entity instanceof Player)&&!(entity instanceof Animal)&&!(entity instanceof Enemy)&&!this.shouldTargetNeutrals()&&(entity instanceof LivingEntity)) {
            info("Entity is neutral, and we don't target them");
            return false;
        }

        return true;
    }
}
