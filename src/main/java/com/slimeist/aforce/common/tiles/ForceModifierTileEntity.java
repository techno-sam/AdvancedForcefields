package com.slimeist.aforce.common.tiles;


import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.containers.force_modifier.ContainerForceModifier;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierStateData;
import com.slimeist.aforce.common.containers.force_modifier.ForceModifierZoneContents;
import com.slimeist.aforce.common.modifier_actions.BlockAction;
import com.slimeist.aforce.common.registries.ForceModifierRegistry;
import com.slimeist.aforce.common.tiles.helpers.ForceModifierSelector;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.ModifierInit;
import com.slimeist.aforce.core.init.RegistryInit;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import com.slimeist.aforce.core.util.TagUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.TileEntity;
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

public class ForceModifierTileEntity extends ForceNetworkTileEntity implements INamedContainerProvider {

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

    private ForceModifierZoneContents upgradeZoneContents;

    private final ForceModifierStateData forceModifierStateData = new ForceModifierStateData();

    public ForceModifierTileEntity() {
        super(TileEntityTypeInit.FORCE_MODIFIER_TYPE);
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

    @Override
    public void tick() {
        super.tick();
        if (this.sendActionsCountdown==0) {
            AdvancedForcefields.LOGGER.info("sendActionsCountdown is zero!");
            for (ForceModifierRegistry action : this.actions) {
                ForceModifierSelector selector = new ForceModifierSelector(this.targetList, this.whitelist, this.targetAnimals, this.targetPlayers, this.targetNeutrals, action.getRegistryName().toString(), priority, this.getBlockPos());
                CompoundNBT message = selector.toNBT();

                CompoundNBT data = new CompoundNBT();
                data.putString(TAG_PACKET_TYPE, "ADD_ACTION");
                data.put(TAG_PACKET_MESSAGE, message);

                this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_MASTER, data));
                AdvancedForcefields.LOGGER.info("Sent ADD_ACTION packet with action of: "+action.getRegistryName().toString());
            }
        }
        if (this.sendActionsCountdown>=0) {
            this.sendActionsCountdown -= 1;
        }
    }

    public void handleUpgrades() {
        this.setChanged();

        Item item = this.upgradeZoneContents.getItem(0).getItem();

        this.actions.clear();

        for (ResourceLocation id : RegistryInit.MODIFIER_REGISTRY.getKeys()) {
            ForceModifierRegistry reg = RegistryInit.MODIFIER_REGISTRY.getValue(id);
            if (reg != null && item == reg.getTrigger()) {
                this.actions.add(reg);
            }
        }

        if (this.actions.size()<=0) {
            this.actions.add(ModifierInit.DEFAULT_ACTION);
        }

        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "CLEAR_ACTIONS");
        data.put(TAG_PACKET_MESSAGE, TagUtil.writePos(this.getBlockPos()));

        this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_MASTER, data));
        this.sendActionsCountdown = 5;

        this.markAsDirty();
        this.markDirtyFast();
    }

    private static void log(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    // Unlike the vanilla furnace, we allow anything to be placed in the input slots
    static public boolean isItemValidForUpgradeSlot(ItemStack itemStack)
    {
        return itemStack.getItem().is(AdvancedForcefieldsTags.Items.MODIFIER_UPGRADE);
    }


    private final String UPGRADE_SLOTS_NBT = "upgradeSlots";

    @Override
    public void loadPersonal(BlockState state, CompoundNBT nbt) {
        super.loadPersonal(state, nbt);
        forceModifierStateData.readFromNBT(nbt);

        CompoundNBT inventoryNBT = nbt.getCompound(UPGRADE_SLOTS_NBT);
        upgradeZoneContents.deserializeNBT(inventoryNBT);

        if (nbt.contains(TAG_OWNER_NAME, Constants.NBT.TAG_STRING)) {
            owner = nbt.getString(TAG_OWNER_NAME);
        }

        ListNBT list = nbt.getList(TAG_TARGET_LIST, Constants.NBT.TAG_STRING);
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
    public void writeSyncedPersonal(CompoundNBT nbt) {
        super.writeSyncedPersonal(nbt);
        forceModifierStateData.putIntoNBT(nbt);
        nbt.put(UPGRADE_SLOTS_NBT, upgradeZoneContents.serializeNBT());
        if (owner!=null) {
            nbt.putString(TAG_OWNER_NAME, owner);
        }
        ListNBT list = new ListNBT();
        for(String s : targetList)
            list.add(StringNBT.valueOf(s));
        nbt.put(TAG_TARGET_LIST, list);
        nbt.putBoolean(TAG_WHITELIST, whitelist);
        nbt.putBoolean(TAG_TARGET_ANIMALS, targetAnimals);
        nbt.putBoolean(TAG_TARGET_PLAYERS, targetPlayers);
        nbt.putBoolean(TAG_TARGET_NEUTRALS, targetNeutrals);
        nbt.putInt(TAG_PRIORITY, priority);
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
                upgradeZoneContents, forceModifierStateData, (TileEntity) this);
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

    public void receiveMessageFromServer(CompoundNBT nbt) {
        log("Received message from server: "+nbt);
    }

    public void receiveMessageFromClient(CompoundNBT nbt) {
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
        log("Received message from client: "+nbt);
        this.markDirtyFast();
    }
}