package com.slimeist.aforce.common.tiles;


import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierStateData;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.common.tiles.helpers.SimpleForceModifierSelector;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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

    public SimpleForceModifierTileEntity(BlockPos pos, BlockState state) {
        super(TileEntityTypeInit.FORCE_MODIFIER_TYPE, pos, state);
    }

    public static <T extends SimpleForceModifierTileEntity> void tick(Level level, BlockPos pos, BlockState state, T tile) {
        ForceNetworkTileEntity.networkTick(level, pos, state, tile);
        if (tile.sendActionsCountdown==0) {
            AdvancedForcefields.LOGGER.info("sendActionsCountdown is zero!");
            for (ForceModifierRegistry action : tile.actions) {
                SimpleForceModifierSelector selector = new SimpleForceModifierSelector(tile.targetList, tile.whitelist, tile.targetAnimals, tile.targetPlayers, tile.targetNeutrals, action.getRegistryName().toString(), tile.priority, tile.getBlockPos(), tile.upgradeZoneContents.getItem(0));
                CompoundTag message = selector.toNBT();

                CompoundTag data = new CompoundTag();
                data.putString(TAG_PACKET_TYPE, "ADD_ACTION");
                data.put(TAG_PACKET_MESSAGE, message);

                tile.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_MASTER, data, tile.getBlockPos()));
                AdvancedForcefields.LOGGER.info("Sent ADD_ACTION packet with action of: "+action.getRegistryName().toString());
            }
        }
        if (tile.sendActionsCountdown>=0) {
            tile.sendActionsCountdown -= 1;
        }
    }

    private static void log(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    @Override
    public void loadPersonal(CompoundTag nbt) {
        super.loadPersonal(nbt);
        forceModifierStateData.readFromNBT(nbt);

        ListTag list = nbt.getList(TAG_TARGET_LIST, Tag.TAG_STRING);
        targetList.clear();
        for (int i = 0; i < list.size(); i++)
            targetList.add(list.getString(i));

        whitelist = nbt.getBoolean(TAG_WHITELIST);
        targetAnimals = nbt.getBoolean(TAG_TARGET_ANIMALS);
        targetPlayers = nbt.getBoolean(TAG_TARGET_PLAYERS);
        targetNeutrals = nbt.getBoolean(TAG_TARGET_NEUTRALS);
    }

    @Override
    public void writeSyncedPersonal(CompoundTag nbt) {
        super.writeSyncedPersonal(nbt);
        forceModifierStateData.putIntoNBT(nbt);
        ListTag list = new ListTag();
        for(String s : targetList)
            list.add(StringTag.valueOf(s));
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
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        return ContainerForceModifier.createContainerServerSide(windowID, playerInventory,
                upgradeZoneContents, forceModifierStateData, (BlockEntity) this);
    }

    /**
     * End of container handling
     */

    @Override
    public void onReceiveToServantsPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {
        super.onReceiveToServantsPacket(myPos, myDist, packet);
    }

    @Override
    public void receiveMessageFromServer(CompoundTag nbt) {
        super.receiveMessageFromServer(nbt);
        log("Received message from server: "+nbt);
    }

    @Override
    public void receiveMessageFromClient(Player from, CompoundTag nbt) {
        super.receiveMessageFromClient(from, nbt);
        if (!this.hasOwnerRights(from))
            return;
        if(nbt.contains("add", Tag.TAG_STRING))
            targetList.add(nbt.getString("add"));
        if(nbt.contains("remove", Tag.TAG_INT))
            targetList.remove(nbt.getInt("remove"));
        if(nbt.contains(TAG_WHITELIST, Tag.TAG_BYTE))
            whitelist = nbt.getBoolean(TAG_WHITELIST);
        if(nbt.contains(TAG_TARGET_ANIMALS, Tag.TAG_BYTE))
            targetAnimals = nbt.getBoolean(TAG_TARGET_ANIMALS);
        if(nbt.contains(TAG_TARGET_PLAYERS, Tag.TAG_BYTE))
            targetPlayers = nbt.getBoolean(TAG_TARGET_PLAYERS);
        if(nbt.contains(TAG_TARGET_NEUTRALS, Tag.TAG_BYTE))
            targetNeutrals = nbt.getBoolean(TAG_TARGET_NEUTRALS);
        if(nbt.contains(TAG_PRIORITY, Tag.TAG_INT))
            priority = nbt.getInt(TAG_PRIORITY);
        log("Received message from client: "+nbt);
        this.handleUpgrades();
        this.markDirtyFast();
    }
}