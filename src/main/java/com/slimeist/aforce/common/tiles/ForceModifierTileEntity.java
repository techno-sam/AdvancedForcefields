package com.slimeist.aforce.common.tiles;


import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierStateData;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierZoneContents;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.common.tiles.helpers.ForceModifierSelector;
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
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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

    public static String TAG_TARGET_LIST = ForceModifierSelector.TAG_TARGET_LIST;
    public List<String> targetList = new ArrayList<>();
    public static String TAG_WHITELIST = ForceModifierSelector.TAG_WHITELIST;
    public boolean whitelist = false;


    public static String TAG_TARGET_ANIMALS = ForceModifierSelector.TAG_TARGET_ANIMALS;
    public boolean targetAnimals = false;

    public static String TAG_TARGET_PLAYERS = ForceModifierSelector.TAG_TARGET_PLAYERS;
    public boolean targetPlayers = false;

    public static String TAG_TARGET_NEUTRALS = ForceModifierSelector.TAG_TARGET_NEUTRALS;
    public boolean targetNeutrals = false;

    public static String TAG_PRIORITY = ForceModifierSelector.TAG_PRIORITY;
    public int priority = 0;

    public List<ForceModifierRegistry> actions = new ArrayList<>();

    protected int sendActionsCountdown = -1;

    protected ForceModifierZoneContents getUpgradeZoneContents() {
        return upgradeZoneContents;
    }

    private ForceModifierZoneContents upgradeZoneContents;

    private final ForceModifierStateData forceModifierStateData = new ForceModifierStateData();

    public ForceModifierTileEntity(BlockPos pos, BlockState state) {
        super(TileEntityTypeInit.FORCE_MODIFIER_TYPE, pos, state);
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

    public static <T extends ForceModifierTileEntity> void tick(Level level, BlockPos pos, BlockState state, T tile) {
        ForceNetworkTileEntity.networkTick(level, pos, state, tile);
        if (tile.sendActionsCountdown==0) {
            AdvancedForcefields.LOGGER.info("sendActionsCountdown is zero!");
            for (ForceModifierRegistry action : tile.actions) {
                ForceModifierSelector selector = new ForceModifierSelector(tile.targetList, tile.whitelist, tile.targetAnimals, tile.targetPlayers, tile.targetNeutrals, action.getRegistryName().toString(), tile.priority, tile.getBlockPos(), tile.getUpgradeZoneContents().getItem(0));
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

    private static void log(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    // Unlike the vanilla furnace, we allow anything to be placed in the input slots
    static public boolean isItemValidForUpgradeSlot(ItemStack itemStack)
    {
        return itemStack.is(AdvancedForcefieldsTags.Items.MODIFIER_UPGRADE);
    }


    private final String UPGRADE_SLOTS_NBT = "upgradeSlots";

    @Override
    public void loadPersonal(CompoundTag nbt) {
        super.loadPersonal(nbt);
        forceModifierStateData.readFromNBT(nbt);

        CompoundTag inventoryNBT = nbt.getCompound(UPGRADE_SLOTS_NBT);
        upgradeZoneContents.deserializeNBT(inventoryNBT);

        if (nbt.contains(TAG_OWNER_NAME, Tag.TAG_STRING)) {
            owner = nbt.getString(TAG_OWNER_NAME);
        }

        ListTag list = nbt.getList(TAG_TARGET_LIST, Tag.TAG_STRING);
        targetList.clear();
        for (int i = 0; i < list.size(); i++)
            targetList.add(list.getString(i));

        whitelist = nbt.getBoolean(TAG_WHITELIST);
        targetAnimals = nbt.getBoolean(TAG_TARGET_ANIMALS);
        targetPlayers = nbt.getBoolean(TAG_TARGET_PLAYERS);
        targetNeutrals = nbt.getBoolean(TAG_TARGET_NEUTRALS);

        priority = nbt.getInt(TAG_PRIORITY);

        if (upgradeZoneContents.getContainerSize() != UPGRADE_SLOTS_COUNT) {
            throw new IllegalArgumentException("Corrupted NBT: Number of inventory slots did not match expected.");
        }
    }

    @Override
    public void writeSyncedPersonal(CompoundTag nbt) {
        super.writeSyncedPersonal(nbt);
        forceModifierStateData.putIntoNBT(nbt);
        nbt.put(UPGRADE_SLOTS_NBT, upgradeZoneContents.serializeNBT());
        if (owner!=null) {
            nbt.putString(TAG_OWNER_NAME, owner);
        }
        ListTag list = new ListTag();
        for(String s : targetList)
            list.add(StringTag.valueOf(s));
        nbt.put(TAG_TARGET_LIST, list);
        nbt.putBoolean(TAG_WHITELIST, whitelist);
        nbt.putBoolean(TAG_TARGET_ANIMALS, targetAnimals);
        nbt.putBoolean(TAG_TARGET_PLAYERS, targetPlayers);
        nbt.putBoolean(TAG_TARGET_NEUTRALS, targetNeutrals);
        nbt.putInt(TAG_PRIORITY, priority);
    }


    public void dropAllContents(Level world, BlockPos blockPos) {
        Containers.dropContents(world, blockPos, upgradeZoneContents);
    }


    /**
     *  standard code to look up what the human-readable name is.
     *  Can be useful when the tileentity has a customised name (eg "David's footlocker")
     */
    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.aforce.force_modifier");
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

    public void onPowered() {
    }

    public void onDepowered() {
    }

    public void receiveMessageFromServer(CompoundTag nbt) {
        log("Received message from server: "+nbt);
    }

    public void receiveMessageFromClient(Player from, CompoundTag nbt) {
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