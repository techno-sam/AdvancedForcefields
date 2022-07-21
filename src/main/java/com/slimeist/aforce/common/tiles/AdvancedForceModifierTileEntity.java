package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.containers.force_modifier.ContainerAdvancedForceModifier;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.common.tiles.helpers.AdvancedForceModifierSelector;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class AdvancedForceModifierTileEntity extends ForceModifierTileEntity {

    public static final String TAG_ENTITY_SELECTOR = "entitySelector";
    public String entitySelector = "@e";

    public static final String TAG_WHITELIST = "whitelist";
    public boolean whitelist = false;

    public AdvancedForceModifierTileEntity(BlockPos pos, BlockState state) {
        super(TileEntityTypeInit.ADVANCED_FORCE_MODIFIER_TYPE, pos, state);
    }
    
    public static <T extends AdvancedForceModifierTileEntity> void tick(Level level, BlockPos pos, BlockState state, T tile) {
        ForceNetworkTileEntity.networkTick(level, pos, state, tile);
        if (tile.sendActionsCountdown==0) {
            AdvancedForcefields.LOGGER.info("sendActionsCountdown is zero!");
            for (ForceModifierRegistry action : tile.actions) {
                AdvancedForceModifierSelector selector = new AdvancedForceModifierSelector(tile.whitelist, tile.entitySelector, action.getRegistryName().toString(), tile.priority, tile.getBlockPos(), tile.upgradeZoneContents.getItem(0));
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

    @Override
    public void loadPersonal(CompoundTag nbt) {
        super.loadPersonal(nbt);
        whitelist = nbt.getBoolean(TAG_WHITELIST);
        entitySelector = nbt.getString(TAG_ENTITY_SELECTOR);
    }

    @Override
    public void writeSyncedPersonal(CompoundTag nbt) {
        super.writeSyncedPersonal(nbt);
        nbt.putBoolean(TAG_WHITELIST, whitelist);
        nbt.putString(TAG_ENTITY_SELECTOR, entitySelector);
    }

    @Override
    public void receiveMessageFromClient(Player from, CompoundTag nbt) {
        super.receiveMessageFromClient(from, nbt);
        if (!this.hasOwnerRights(from))
            return;
        if(nbt.contains("set", Tag.TAG_STRING))
            entitySelector = nbt.getString("set");
        if(nbt.contains(TAG_WHITELIST, Tag.TAG_BYTE))
            whitelist = nbt.getBoolean(TAG_WHITELIST);
        if(nbt.contains(TAG_PRIORITY, Tag.TAG_INT))
            priority = nbt.getInt(TAG_PRIORITY);
        //log("Received message from client: "+nbt);
        this.handleUpgrades();
        this.markDirtyFast();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowID, Inventory playerInventory, Player playerEntity) {
        return ContainerAdvancedForceModifier.createContainerServerSide(windowID, playerInventory,
                upgradeZoneContents, (BlockEntity) this);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.aforce.advanced_force_modifier");
    }
}
