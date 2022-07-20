package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.containers.force_modifier.ContainerAdvancedForceModifier;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.common.tiles.helpers.AdvancedForceModifierSelector;
import com.slimeist.aforce.common.tiles.helpers.SimpleForceModifierSelector;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class AdvancedForceModifierTileEntity extends ForceModifierTileEntity {

    public static final String TAG_ENTITY_SELECTOR = "entitySelector";
    public String entitySelector = "@e";

    public static final String TAG_WHITELIST = "whitelist";
    public boolean whitelist = false;

    public AdvancedForceModifierTileEntity() {
        super(TileEntityTypeInit.ADVANCED_FORCE_MODIFIER_TYPE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.sendActionsCountdown==0) {
            AdvancedForcefields.LOGGER.info("sendActionsCountdown is zero!");
            for (ForceModifierRegistry action : this.actions) {
                AdvancedForceModifierSelector selector = new AdvancedForceModifierSelector(this.whitelist, this.entitySelector, action.getRegistryName().toString(), priority, this.getBlockPos(), this.upgradeZoneContents.getItem(0));
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

    @Override
    public void loadPersonal(BlockState blockState, CompoundNBT nbt) {
        super.loadPersonal(blockState, nbt);
        whitelist = nbt.getBoolean(TAG_WHITELIST);
        entitySelector = nbt.getString(TAG_ENTITY_SELECTOR);
    }

    @Override
    public void writeSyncedPersonal(CompoundNBT nbt) {
        super.writeSyncedPersonal(nbt);
        nbt.putBoolean(TAG_WHITELIST, whitelist);
        nbt.putString(TAG_ENTITY_SELECTOR, entitySelector);
    }

    @Override
    public void receiveMessageFromClient(PlayerEntity from, CompoundNBT nbt) {
        super.receiveMessageFromClient(from, nbt);
        if (!this.hasOwnerRights(from))
            return;
        if(nbt.contains("set", Constants.NBT.TAG_STRING))
            entitySelector = nbt.getString("set");
        if(nbt.contains(TAG_WHITELIST, Constants.NBT.TAG_BYTE))
            whitelist = nbt.getBoolean(TAG_WHITELIST);
        if(nbt.contains(TAG_PRIORITY, Constants.NBT.TAG_INT))
            priority = nbt.getInt(TAG_PRIORITY);
        //log("Received message from client: "+nbt);
        this.handleUpgrades();
        this.markDirtyFast();
    }

    @Nullable
    @Override
    public Container createMenu(int windowID, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return ContainerAdvancedForceModifier.createContainerServerSide(windowID, playerInventory,
                upgradeZoneContents, (TileEntity) this);
    }
}
