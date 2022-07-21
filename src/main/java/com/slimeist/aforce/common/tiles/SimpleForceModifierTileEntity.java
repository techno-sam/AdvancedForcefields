package com.slimeist.aforce.common.tiles;


import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierStateData;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierZoneContents;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.common.tiles.helpers.BaseForceModifierSelector;
import com.slimeist.aforce.common.tiles.helpers.SimpleForceModifierSelector;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.ModifierInit;
import com.slimeist.aforce.core.init.RegistryInit;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import com.slimeist.aforce.core.util.TagUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Filterable upgrades:
 * Slime block - makes blocks bouncy, and knocks entities back (if force field solid for entity)
 * Magma block - makes blocks damage entities on & in them (like magma blocks) (unconditional on force field solidity for entity)
 * Any lingering potion - acts as if entity walked through lingering cloud (uncoditional on force field solidity for entity,
 *                                                                          range of 1 block beyond force field,
 *                                                                          if entity is player (OR the entity is being spectated) will spawn particles, clientside only)
 * Blaze rod - sets entities on fire (unconditional on force field solidity for entity)
 *
 * Constant upgrades: (Handled by ForceController)
 * Tinted glasses - combines colors to set forcefield color
 */

public class SimpleForceModifierTileEntity extends ForceModifierTileEntity {

    public static String TAG_TARGET_LIST = SimpleForceModifierSelector.TAG_TARGET_LIST;
    public List<String> targetList = new ArrayList<>();
    public static String TAG_WHITELIST = SimpleForceModifierSelector.TAG_WHITELIST;
    public boolean whitelist = false;


    public static String TAG_TARGET_ANIMALS = SimpleForceModifierSelector.TAG_TARGET_ANIMALS;
    public boolean targetAnimals = false;

    public static String TAG_TARGET_PLAYERS = SimpleForceModifierSelector.TAG_TARGET_PLAYERS;
    public boolean targetPlayers = false;

    public static String TAG_TARGET_NEUTRALS = SimpleForceModifierSelector.TAG_TARGET_NEUTRALS;
    public boolean targetNeutrals = false;

    private final ForceModifierStateData forceModifierStateData = new ForceModifierStateData();

    public SimpleForceModifierTileEntity() {
        super(TileEntityTypeInit.FORCE_MODIFIER_TYPE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.sendActionsCountdown==0) {
            AdvancedForcefields.LOGGER.info("sendActionsCountdown is zero!");
            for (ForceModifierRegistry action : this.actions) {
                SimpleForceModifierSelector selector = new SimpleForceModifierSelector(this.targetList, this.whitelist, this.targetAnimals, this.targetPlayers, this.targetNeutrals, action.getRegistryName().toString(), priority, this.getBlockPos(), this.upgradeZoneContents.getItem(0));
                CompoundNBT message = selector.toNBT();

                CompoundNBT data = new CompoundNBT();
                data.putString(TAG_PACKET_TYPE, "ADD_ACTION");
                data.put(TAG_PACKET_MESSAGE, message);

                this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_MASTER, data, this.getBlockPos()));
                AdvancedForcefields.LOGGER.info("Sent ADD_ACTION packet with action of: "+action.getRegistryName().toString());
            }
        }
        if (this.sendActionsCountdown>=0) {
            this.sendActionsCountdown -= 1;
        }
    }

    private static void log(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    @Override
    public void loadPersonal(BlockState state, CompoundNBT nbt) {
        super.loadPersonal(state, nbt);
        forceModifierStateData.readFromNBT(nbt);

        ListNBT list = nbt.getList(TAG_TARGET_LIST, Constants.NBT.TAG_STRING);
        targetList.clear();
        for (int i = 0; i < list.size(); i++)
            targetList.add(list.getString(i));

        whitelist = nbt.getBoolean(TAG_WHITELIST);
        targetAnimals = nbt.getBoolean(TAG_TARGET_ANIMALS);
        targetPlayers = nbt.getBoolean(TAG_TARGET_PLAYERS);
        targetNeutrals = nbt.getBoolean(TAG_TARGET_NEUTRALS);
    }

    @Override
    public void writeSyncedPersonal(CompoundNBT nbt) {
        super.writeSyncedPersonal(nbt);
        forceModifierStateData.putIntoNBT(nbt);
        ListNBT list = new ListNBT();
        for(String s : targetList)
            list.add(StringNBT.valueOf(s));
        nbt.put(TAG_TARGET_LIST, list);
        nbt.putBoolean(TAG_WHITELIST, whitelist);
        nbt.putBoolean(TAG_TARGET_ANIMALS, targetAnimals);
        nbt.putBoolean(TAG_TARGET_PLAYERS, targetPlayers);
        nbt.putBoolean(TAG_TARGET_NEUTRALS, targetNeutrals);
    }

    /**
     * The name is misleading; createMenu has nothing to do with creating a Screen, it is used to create the Container on the server only
     * @param windowID
     * @param playerInventory
     * @param playerEntity
     * @return
     */
    @Nullable
    @Override
    public Container createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return ContainerForceModifier.createContainerServerSide(windowID, playerInventory,
                upgradeZoneContents, forceModifierStateData, (TileEntity) this);
    }

    /**
     * End of container handling
     */

    @Override
    public void onReceiveToServantsPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {
        super.onReceiveToServantsPacket(myPos, myDist, packet);
    }

    @Override
    public void receiveMessageFromServer(CompoundNBT nbt) {
        super.receiveMessageFromServer(nbt);
        log("Received message from server: "+nbt);
    }

    @Override
    public void receiveMessageFromClient(PlayerEntity from, CompoundNBT nbt) {
        super.receiveMessageFromClient(from, nbt);
        if (!this.hasOwnerRights(from))
            return;
        if(nbt.contains("add", Constants.NBT.TAG_STRING))
            targetList.add(nbt.getString("add"));
        if(nbt.contains("remove", Constants.NBT.TAG_INT))
            targetList.remove(nbt.getInt("remove"));
        if(nbt.contains(TAG_WHITELIST, Constants.NBT.TAG_BYTE))
            whitelist = nbt.getBoolean(TAG_WHITELIST);
        if(nbt.contains(TAG_TARGET_ANIMALS, Constants.NBT.TAG_BYTE))
            targetAnimals = nbt.getBoolean(TAG_TARGET_ANIMALS);
        if(nbt.contains(TAG_TARGET_PLAYERS, Constants.NBT.TAG_BYTE))
            targetPlayers = nbt.getBoolean(TAG_TARGET_PLAYERS);
        if(nbt.contains(TAG_TARGET_NEUTRALS, Constants.NBT.TAG_BYTE))
            targetNeutrals = nbt.getBoolean(TAG_TARGET_NEUTRALS);
        if(nbt.contains(TAG_PRIORITY, Constants.NBT.TAG_INT))
            priority = nbt.getInt(TAG_PRIORITY);
        log("Received message from client: "+nbt);
        this.handleUpgrades();
        this.markDirtyFast();
    }
}