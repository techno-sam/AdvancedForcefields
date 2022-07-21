package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ForceModifierTileEntity extends ForceNetworkTileEntity implements MenuProvider {

    protected boolean shouldSignal = true;

    public boolean isSignalling() {
        return this.shouldSignal;
    }

    public void setSignalling(boolean shouldSignal) {
        this.shouldSignal = shouldSignal;
    }

    public void networkDisconnect() {
        CompoundTag data = new CompoundTag();
        data.putString(TAG_PACKET_TYPE, "NETWORK_RELEASE");
        data.put(TAG_PACKET_MESSAGE, new CompoundTag());
        ForceNetworkPacket release_packet = new ForceNetworkPacket(ForceNetworkDirection.TO_SERVANTS, data, this.getBlockPos(), true);
        this.onReceiveToServantsPacket(this.getBlockPos(), this.getDistance(), release_packet);
    }

    public String owner;

    public static final String TAG_OWNER_NAME = "owner";

    public static final int UPGRADE_SLOTS_COUNT = 1;

    public static final int TOTAL_SLOTS_COUNT = UPGRADE_SLOTS_COUNT; //upgrade slots

    public static String TAG_PRIORITY = BaseForceModifierSelector.TAG_PRIORITY;
    public int priority = 0;

    public List<ForceModifierRegistry> actions = new ArrayList<>();

    protected int sendActionsCountdown = -1;

    protected ForceModifierZoneContents getUpgradeZoneContents() {
        return upgradeZoneContents;
    }

    protected ForceModifierZoneContents upgradeZoneContents;

    protected ForceModifierTileEntity(BlockEntityType<?> tileEntityType, BlockPos pos, BlockState state) {
        super(tileEntityType, pos, state);
        upgradeZoneContents = ForceModifierZoneContents.createForTileEntity(UPGRADE_SLOTS_COUNT,
                this::canPlayerAccessInventory, this::handleUpgrades);
    }

    protected boolean hasOwnerRights(Player player)
    {
        if(player.getAbilities().instabuild||owner==null||owner.isEmpty())
            return true;
        return owner.equalsIgnoreCase(player.getName().getString());
    }

    public boolean canEntityDestroy(Entity entity) {
        if(entity instanceof Player)
            return hasOwnerRights((Player)entity);
        return owner==null||owner.isEmpty();
    }

    public boolean canUseGui(Player player)
    {
        if(hasOwnerRights(player))
            return true;
        player.displayClientMessage(new TranslatableComponent("container.isLocked", this.getDisplayName()), true);
        player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 1.0F, 1.0F);
        return false;
    }

    /**
     * Start of container handling
     */

    // Return true if the given player is able to use this block. In this case it checks that
    // 1) the world tileentity hasn't been replaced in the meantime, and
    // 2) the player isn't too far away from the centre of the block
    public boolean canPlayerAccessInventory(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) return false;
        final double X_CENTRE_OFFSET = 0.5;
        final double Y_CENTRE_OFFSET = 0.5;
        final double Z_CENTRE_OFFSET = 0.5;
        final double MAXIMUM_DISTANCE_SQ = 8.0 * 8.0;
        return player.distanceToSqr(worldPosition.getX() + X_CENTRE_OFFSET, worldPosition.getY() + Y_CENTRE_OFFSET, worldPosition.getZ() + Z_CENTRE_OFFSET) < MAXIMUM_DISTANCE_SQ;
    }


    /**
     *  standard code to look up what the human-readable name is.
     *  Can be useful when the tileentity has a customised name (eg "David's footlocker")
     */
    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.aforce.force_modifier");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_, Player p_createMenu_3_) {
        return null;
    }

    public void handleUpgrades() {
        this.setChanged();

        ItemStack stack = this.upgradeZoneContents.getItem(0);

        this.actions.clear();
        this.clearActionSelectors(this.getBlockPos());

        for (ResourceLocation id : RegistryInit.MODIFIER_REGISTRY.getKeys()) {
            ForceModifierRegistry reg = RegistryInit.MODIFIER_REGISTRY.getValue(id);
            if (reg != null && reg.matches(stack)) {
                this.actions.add(reg);
            }
        }

        if (this.actions.size()<=0) {
            this.actions.add(ModifierInit.DEFAULT_ACTION);
        }

        CompoundTag data = new CompoundTag();
        data.putString(TAG_PACKET_TYPE, "CLEAR_ACTIONS");
        data.put(TAG_PACKET_MESSAGE, TagUtil.writePos(this.getBlockPos()));

        this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_MASTER, data, this.getBlockPos()));
        this.sendActionsCountdown = 5;

        //this.markAsDirty(); //unnecessary, no shared data changed
        this.markDirtyFast();
    }

    @Override
    public void postNetworkBuild() {
        super.postNetworkBuild();
        this.handleUpgrades();
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    // Unlike the vanilla furnace, we allow anything to be placed in the input slots
    static public boolean isItemValidForUpgradeSlot(ItemStack itemStack)
    {
        return itemStack.is(AdvancedForcefieldsTags.Items.MODIFIER_UPGRADE);
    }

    protected final String UPGRADE_SLOTS_NBT = "upgradeSlots";

    @Override
    public void loadPersonal(CompoundTag nbt) {
        super.loadPersonal(nbt);

        CompoundTag inventoryNBT = nbt.getCompound(UPGRADE_SLOTS_NBT);
        upgradeZoneContents.deserializeNBT(inventoryNBT);

        if (nbt.contains(TAG_OWNER_NAME, Tag.TAG_STRING)) {
            owner = nbt.getString(TAG_OWNER_NAME);
        }

        priority = nbt.getInt(TAG_PRIORITY);

        if (upgradeZoneContents.getContainerSize() != UPGRADE_SLOTS_COUNT) {
            throw new IllegalArgumentException("Corrupted NBT: Number of inventory slots did not match expected.");
        }
    }

    @Override
    public void writeSyncedPersonal(CompoundTag nbt) {
        super.writeSyncedPersonal(nbt);
        nbt.put(UPGRADE_SLOTS_NBT, upgradeZoneContents.serializeNBT());
        if (owner!=null) {
            nbt.putString(TAG_OWNER_NAME, owner);
        }
        nbt.putInt(TAG_PRIORITY, priority);
    }

    public void dropAllContents(Level world, BlockPos blockPos) {
        Containers.dropContents(world, blockPos, upgradeZoneContents);
    }

    public void onPowered() {}

    public void onDepowered() {}

    public void receiveMessageFromServer(CompoundTag nbt) {}

    public void receiveMessageFromClient(Player from, CompoundTag nbt) {}
}
