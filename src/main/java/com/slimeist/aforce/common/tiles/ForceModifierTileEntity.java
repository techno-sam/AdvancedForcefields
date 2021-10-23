package com.slimeist.aforce.common.tiles;


import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.containers.force_controller.ForceControllerZoneContents;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierStateData;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierZoneContents;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

/**
 * Filterable upgrades:
 * Slime block - makes blocks bouncy, and knocks entities back (only when entities would be blocked)
 * Magma block - makes blocks damage entities on & in them (like magma blocks)
 * Any potion - applies effect for 10 seconds
 * Blaze rod - sets entities on fire
 *
 * Constant upgrades:
 * Tinted glasses - combines colors to set forcefield color
 */

public class ForceModifierTileEntity extends ForceNetworkTileEntity implements INamedContainerProvider {

    public String owner;

    public static final String TAG_OWNER_NAME = "owner";

    public static final int UPGRADE_SLOTS_COUNT = 1;

    public static final int TOTAL_SLOTS_COUNT = UPGRADE_SLOTS_COUNT; //upgrade slots

    private ForceModifierZoneContents upgradeZoneContents;

    private final ForceModifierStateData forceModifierStateData = new ForceModifierStateData();

    public ForceModifierTileEntity() {
        super(TileEntityTypeInit.FORCE_MODIFIER_TYPE);
        upgradeZoneContents = ForceModifierZoneContents.createForTileEntity(UPGRADE_SLOTS_COUNT,
                this::canPlayerAccessInventory, this::setChanged);
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

    @Override
    public void tick() {
        super.tick();
        this.handleUpgrades();
    }

    public void handleUpgrades() {

    }

    private static void log(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    // Unlike the vanilla furnace, we allow anything to be placed in the input slots
    static public boolean isItemValidForUpgradeSlot(ItemStack itemStack)
    {
        return true;//itemStack.getItem().is(Tags.Items.GLASS);
    }


    private final String UPGRADE_SLOTS_NBT = "upgradeSlots";

    @Override
    public void loadPersonal(BlockState state, CompoundNBT nbt) {
        super.loadPersonal(state, nbt);
        forceModifierStateData.readFromNBT(nbt);

        CompoundNBT inventoryNBT = nbt.getCompound(UPGRADE_SLOTS_NBT);
        upgradeZoneContents.deserializeNBT(inventoryNBT);

        if (nbt.contains(TAG_OWNER_NAME, Constants.NBT.TAG_STRING)) {
            this.owner = nbt.getString(TAG_OWNER_NAME);
        }

        if (upgradeZoneContents.getContainerSize() != UPGRADE_SLOTS_COUNT) {
            throw new IllegalArgumentException("Corrupted NBT: Number of inventory slots did not match expected.");
        }
    }

    @Override
    public void writeSyncedPersonal(CompoundNBT nbt) {
        super.writeSyncedPersonal(nbt);
        forceModifierStateData.putIntoNBT(nbt);
        nbt.put(UPGRADE_SLOTS_NBT, upgradeZoneContents.serializeNBT());
        if (this.owner!=null) {
            nbt.putString(TAG_OWNER_NAME, this.owner);
        }
    }


    public void dropAllContents(World world, BlockPos blockPos) {
        InventoryHelper.dropContents(world, blockPos, upgradeZoneContents);
    }


    /**
     *  standard code to look up what the human-readable name is.
     *  Can be useful when the tileentity has a customised name (eg "David's footlocker")
     */
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("container.aforce.force_modifier");
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
                upgradeZoneContents, forceModifierStateData);
    }

    /**
     * End of container handling
     */

    @Override
    public void onReceiveToServantsPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {
        super.onReceiveToServantsPacket(myPos, myDist, packet);
    }

    public void onPowered() {
    }

    public void onDepowered() {
    }
}