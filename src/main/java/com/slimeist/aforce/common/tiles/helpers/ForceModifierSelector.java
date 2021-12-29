package com.slimeist.aforce.common.tiles.helpers;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.core.util.TagUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

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

    public ForceModifierSelector(BlockPos originPosition) {
        this.originPosition = originPosition;
    }

    public ForceModifierSelector(List<String> targetList, boolean whitelist, boolean targetAnimals, boolean targetPlayers, boolean targetNeutrals, String action, int priority, BlockPos originPosition) {
        this.targetList = targetList;
        this.whitelist = whitelist;
        this.targetAnimals = targetAnimals;
        this.targetPlayers = targetPlayers;
        this.targetNeutrals = targetNeutrals;
        this.action = action;
        this.priority = priority;
        this.originPosition = originPosition;
    }

    public static ForceModifierSelector fromNBT(CompoundNBT nbt) {
        ForceModifierSelector selector = new ForceModifierSelector(TagUtil.readPos(nbt.getCompound(TAG_ORIGIN_POSITION)));

        if (nbt.contains(TAG_TARGET_LIST, Constants.NBT.TAG_LIST)) {
            ListNBT list = nbt.getList(TAG_TARGET_LIST, Constants.NBT.TAG_STRING);
            List<String> tlist = new ArrayList<>();
            for (int i=0; i<list.size(); i++) {
                tlist.add(list.getString(i));
            }
            selector.setTargetList(tlist);
        }

        if (nbt.contains(TAG_WHITELIST, Constants.NBT.TAG_BYTE)) {
            selector.setWhitelist(nbt.getBoolean(TAG_WHITELIST));
        }

        if (nbt.contains(TAG_TARGET_ANIMALS, Constants.NBT.TAG_BYTE)) {
            selector.setTargetAnimals(nbt.getBoolean(TAG_TARGET_ANIMALS));
        }

        if (nbt.contains(TAG_TARGET_PLAYERS, Constants.NBT.TAG_BYTE)) {
            selector.setTargetPlayers(nbt.getBoolean(TAG_TARGET_PLAYERS));
        }

        if (nbt.contains(TAG_TARGET_NEUTRALS, Constants.NBT.TAG_BYTE)) {
            selector.setTargetNeutrals(nbt.getBoolean(TAG_TARGET_NEUTRALS));
        }

        if (nbt.contains(TAG_MODIFIER_ACTION, Constants.NBT.TAG_STRING)) {
            selector.setAction(nbt.getString(TAG_MODIFIER_ACTION));
        }

        if (nbt.contains(TAG_PRIORITY, Constants.NBT.TAG_INT)) {
            selector.setPriority(nbt.getInt(TAG_PRIORITY));
        }

        return selector;
    }

    public CompoundNBT toNBT() {
        CompoundNBT nbt = new CompoundNBT();

        ListNBT tlist = new ListNBT();
        for (String target : this.getTargetList()) {
            tlist.add(StringNBT.valueOf(target));
        }
        nbt.put(TAG_TARGET_LIST, tlist);

        nbt.putBoolean(TAG_WHITELIST, this.isWhitelist());

        nbt.putBoolean(TAG_TARGET_ANIMALS, this.shouldTargetAnimals());
        nbt.putBoolean(TAG_TARGET_PLAYERS, this.shouldTargetPlayers());
        nbt.putBoolean(TAG_TARGET_NEUTRALS, this.shouldTargetNeutrals());

        nbt.putString(TAG_MODIFIER_ACTION, this.getAction());
        nbt.putInt(TAG_PRIORITY, this.getPriority());

        nbt.put(TAG_ORIGIN_POSITION, TagUtil.writePos(this.getOriginPosition()));

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
    public boolean validForEntity(Entity entity) {
        info("Checking for entity: "+entity.getName().getString());
        if (entity==null || entity.level==null) {
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

        if (entity instanceof AnimalEntity &&!this.shouldTargetAnimals()) {
            info("Entity is an animal, and we don't target them.");
            return false;
        }
        if (entity instanceof PlayerEntity &&!this.shouldTargetPlayers()) {
            info("Entity is a player, and we don't target them.");
            return false;
        }
        if (!(entity instanceof PlayerEntity)&&!(entity instanceof AnimalEntity)&&!(entity instanceof IMob)&&!this.shouldTargetNeutrals()&&(entity instanceof LivingEntity)) {
            info("Entity is neutral, and we don't target them");
            return false;
        }

        return true;
    }
}
