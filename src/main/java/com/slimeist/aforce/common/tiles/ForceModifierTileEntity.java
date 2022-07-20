package com.slimeist.aforce.common.tiles;

import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierZoneContents;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.common.tiles.helpers.BaseForceModifierSelector;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.ModifierInit;
import com.slimeist.aforce.core.init.RegistryInit;
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
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ForceModifierTileEntity extends ForceNetworkTileEntity implements INamedContainerProvider {

    protected boolean shouldSignal = true;

    public boolean isSignalling() {
        return this.shouldSignal;
    }

    public void setSignalling(boolean shouldSignal) {
        this.shouldSignal = shouldSignal;
    }

    public void networkDisconnect() {
        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "NETWORK_RELEASE");
        data.put(TAG_PACKET_MESSAGE, new CompoundNBT());
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

    protected ForceModifierZoneContents upgradeZoneContents;

    protected ForceModifierTileEntity(TileEntityType<?> tileEntityType) {
        super(tileEntityType);
        upgradeZoneContents = ForceModifierZoneContents.createForTileEntity(UPGRADE_SLOTS_COUNT,
                this::canPlayerAccessInventory, this::handleUpgrades);
    }

    protected boolean hasOwnerRights(PlayerEntity player)
    {
        if(player.abilities.instabuild||owner==null||owner.isEmpty())
            return true;
        return owner.equalsIgnoreCase(player.getName().getString());
    }

    public boolean canEntityDestroy(Entity entity) {
        if(entity instanceof PlayerEntity)
            return hasOwnerRights((PlayerEntity)entity);
        return owner==null||owner.isEmpty();
    }

    public boolean canUseGui(PlayerEntity player)
    {
        if(hasOwnerRights(player))
            return true;
        player.displayClientMessage(new TranslationTextComponent("container.isLocked", this.getDisplayName()), true);
        player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
        return false;
    }

    /**
     * Start of container handling
     */

    // Return true if the given player is able to use this block. In this case it checks that
    // 1) the world tileentity hasn't been replaced in the meantime, and
    // 2) the player isn't too far away from the centre of the block
    public boolean canPlayerAccessInventory(PlayerEntity player) {
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
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("container.aforce.force_modifier");
    }

    @Nullable
    @Override
    public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
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

        CompoundNBT data = new CompoundNBT();
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
        return itemStack.getItem().is(AdvancedForcefieldsTags.Items.MODIFIER_UPGRADE);
    }

    protected final String UPGRADE_SLOTS_NBT = "upgradeSlots";

    @Override
    public void loadPersonal(BlockState blockState, CompoundNBT nbt) {
        super.loadPersonal(blockState, nbt);

        CompoundNBT inventoryNBT = nbt.getCompound(UPGRADE_SLOTS_NBT);
        upgradeZoneContents.deserializeNBT(inventoryNBT);

        if (nbt.contains(TAG_OWNER_NAME, Constants.NBT.TAG_STRING)) {
            owner = nbt.getString(TAG_OWNER_NAME);
        }

        priority = nbt.getInt(TAG_PRIORITY);

        if (upgradeZoneContents.getContainerSize() != UPGRADE_SLOTS_COUNT) {
            throw new IllegalArgumentException("Corrupted NBT: Number of inventory slots did not match expected.");
        }
    }

    @Override
    public void writeSyncedPersonal(CompoundNBT nbt) {
        super.writeSyncedPersonal(nbt);
        nbt.put(UPGRADE_SLOTS_NBT, upgradeZoneContents.serializeNBT());
        if (owner!=null) {
            nbt.putString(TAG_OWNER_NAME, owner);
        }
        nbt.putInt(TAG_PRIORITY, priority);
    }

    public void dropAllContents(World world, BlockPos blockPos) {
        InventoryHelper.dropContents(world, blockPos, upgradeZoneContents);
    }

    public void onPowered() {}

    public void onDepowered() {}

    public void receiveMessageFromServer(CompoundNBT nbt) {}

    public void receiveMessageFromClient(PlayerEntity from, CompoundNBT nbt) {}
}
