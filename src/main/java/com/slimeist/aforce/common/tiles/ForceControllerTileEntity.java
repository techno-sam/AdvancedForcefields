package com.slimeist.aforce.common.tiles;


import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import com.slimeist.aforce.common.StartupCommon;
import com.slimeist.aforce.common.blocks.ForceTubeBlock;
import com.slimeist.aforce.common.containers.force_controller.ContainerForceController;
import com.slimeist.aforce.common.containers.force_controller.ForceControllerStateData;
import com.slimeist.aforce.common.containers.force_controller.ForceControllerZoneContents;
import com.slimeist.aforce.common.recipies.EnderFuelRecipe;
import com.slimeist.aforce.common.tiles.helpers.ForceModifierSelector;
import com.slimeist.aforce.core.enums.ForceNetworkDirection;
import com.slimeist.aforce.core.init.TileEntityTypeInit;
import com.slimeist.aforce.core.util.ColorUtil;
import com.slimeist.aforce.core.util.ForceNetworkPacket;
import com.slimeist.aforce.core.util.NetworkBlockChain;
import com.slimeist.aforce.core.util.TagUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IBeaconBeamColorProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Filterable upgrades: (Handled by ForceModifier)
 * Slime block - makes blocks bouncy, and knocks entities back (only when entities would be blocked)
 * Magma block - makes blocks damage entities on & in them (like magma blocks)
 * Any potion - applies effect for 10 seconds
 * Blaze rod - sets entities on fire
 *
 * Constant upgrades:
 * Tinted glasses - combines colors to set forcefield color
 */

public class ForceControllerTileEntity extends ForceNetworkTileEntity implements INamedContainerProvider {

    public String owner;

    public static final String TAG_OWNER_NAME = "owner";

    public static final int GLASS_SLOTS_COUNT = 16;

    public static final int TOTAL_SLOTS_COUNT = GLASS_SLOTS_COUNT + 1; //glass slots and fuel slot

    private ForceControllerZoneContents fuelZoneContents;
    private ForceControllerZoneContents glassZoneContents;

    private final ForceControllerStateData forceControllerStateData = new ForceControllerStateData();

    public ForceControllerTileEntity() {
        super(TileEntityTypeInit.FORCE_CONTROLLER_TYPE);
        fuelZoneContents = ForceControllerZoneContents.createForTileEntity(1,
                this::canPlayerAccessInventory, this::setChanged);
        glassZoneContents = ForceControllerZoneContents.createForTileEntity(GLASS_SLOTS_COUNT,
                this::canPlayerAccessInventory, this::glassSlotsChanged);
    }

    protected boolean hasOwnerRights(PlayerEntity player) {
        if (player.abilities.instabuild || owner == null || owner.isEmpty())
            return true;
        return owner.equalsIgnoreCase(player.getName().getString());
    }

    public boolean canEntityDestroy(Entity entity) {
        if (entity instanceof PlayerEntity)
            return hasOwnerRights((PlayerEntity) entity);
        return owner == null || owner.isEmpty();
    }

    public boolean canUseGui(PlayerEntity player) {
        if (hasOwnerRights(player))
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

    public boolean hasBurningFuelSlots() {
        return forceControllerStateData.burnTimeRemaining > 0;
    }

    @Override
    public void tick() {
        super.tick();
        this.handleFuel();
    }

    public void glassSlotsChanged() {
        this.updateStainedGlass();
        this.setChanged();
    }

    public void updateStainedGlass() {
        int alpha = 255;
        int totalColors = 0;
        float[] colorSum = new float[]{0.0f, 0.0f, 0.0f};

        int slots = this.glassZoneContents.getContainerSize();
        for (int slot = 0; slot < slots; slot++) {
            ItemStack stack = this.glassZoneContents.getItem(slot);
            Item item = stack.getItem();
            if (Tags.Items.GLASS_COLORLESS.contains(item)) {
                alpha -= stack.getCount();
            }
            if (item instanceof BlockItem) {
                Block block = ((BlockItem) item).getBlock();
                if (block instanceof IBeaconBeamColorProvider) {
                    float[] dyeColor = ((IBeaconBeamColorProvider) block).getColor().getTextureDiffuseColors();
                    for (int i = 0; i < stack.getCount(); i++) {
                        totalColors++;
                        colorSum = new float[]{colorSum[0] + dyeColor[0], colorSum[1] + dyeColor[1], colorSum[2] + dyeColor[2]};
                    }
                }
            }
        }

        float[] color = new float[]{colorSum[0] / totalColors, colorSum[1] / totalColors, colorSum[2] / totalColors};

        int red = (int) (color[0] * 255);
        int green = (int) (color[1] * 255);
        int blue = (int) (color[2] * 255);

        if (red < 0) red = 0;
        if (green < 0) green = 0;
        if (blue < 0) blue = 0;
        if (alpha < 0) alpha = 0;

        if (red > 255) red = 255;
        if (green > 255) green = 255;
        if (blue > 255) blue = 255;
        if (alpha > 255) alpha = 255;

        int packedColor = ColorUtil.packRGBA(red, green, blue, alpha);
        this.setColor(packedColor);
        this.markAsDirty();
        this.markDirtyFast();
    }

    public boolean handleFuel() {
        boolean isBurning = false;
        if (!this.level.isClientSide) {
            boolean inventoryChanged = false;
            if (forceControllerStateData.burnTimeRemaining>0) {
                --forceControllerStateData.burnTimeRemaining;
                isBurning = true;
            }

            if (forceControllerStateData.burnTimeRemaining==0) {
                ItemStack fuelStack = fuelZoneContents.getItem(0);
                if (!fuelStack.isEmpty() && getItemBurnTime(this.level, fuelStack) > 0) {
                    int burnTimeForItem = getItemBurnTime(this.level, fuelStack);
                    forceControllerStateData.burnTimeRemaining = burnTimeForItem;
                    forceControllerStateData.burnTimeInitialValue = burnTimeForItem;
                    fuelZoneContents.removeItem(0, 1);
                    inventoryChanged = true;

                    if (fuelStack.isEmpty()) {
                        ItemStack containerItem = fuelStack.getContainerItem();
                        fuelZoneContents.setItem(0, containerItem);
                    }
                }
            }

            if (inventoryChanged) {
                this.setChanged();
            }
        }
        return isBurning;
    }

    public static int getItemBurnTime(World world, ItemStack stack)
    {
        if (world==null) {
            return -1;
        }
        List<EnderFuelRecipe> recipes = world.getRecipeManager().getAllRecipesFor(StartupCommon.ENDER_FUEL_RECIPE);
        for (EnderFuelRecipe recipe : recipes) {
            if (recipe.matches(stack, world)) {
                return recipe.getFuelTicks();
            }
        }
        return -1;
    }

    private static void log(String msg) {
        AdvancedForcefields.LOGGER.info(msg);
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    // Unlike the vanilla furnace, we allow anything to be placed in the fuel slots
    static public boolean isItemValidForFuelSlot(ItemStack itemStack)
    {
        //log("Checking if "+itemStack.toString()+" is part of ENDER_FUEL tag");
        //log("ENDER_FUEL tag contains:");
        for (Item item : AdvancedForcefieldsTags.Items.ENDER_FUEL.getValues()) {
            log(item.toString());
        }
        return itemStack.getItem().is(AdvancedForcefieldsTags.Items.ENDER_FUEL);
    }

    // Return true if the given stack is allowed to be inserted in the given slot
    // Unlike the vanilla furnace, we allow anything to be placed in the input slots
    static public boolean isItemValidForGlassSlot(ItemStack itemStack)
    {
        return itemStack.getItem().is(Tags.Items.GLASS);
    }


    private final String FUEL_SLOTS_NBT = "fuelSlots";
    private final String GLASS_SLOTS_NBT = "glassSlots";

    @Override
    public void loadPersonal(BlockState state, CompoundNBT nbt) {
        super.loadPersonal(state, nbt);
        forceControllerStateData.readFromNBT(nbt);

        CompoundNBT inventoryNBT = nbt.getCompound(FUEL_SLOTS_NBT);
        fuelZoneContents.deserializeNBT(inventoryNBT);

        inventoryNBT = nbt.getCompound(GLASS_SLOTS_NBT);
        glassZoneContents.deserializeNBT(inventoryNBT);

        if (nbt.contains(TAG_OWNER_NAME, Constants.NBT.TAG_STRING)) {
            this.owner = nbt.getString(TAG_OWNER_NAME);
        }

        if (fuelZoneContents.getContainerSize() != 1 || glassZoneContents.getContainerSize() != GLASS_SLOTS_COUNT) {
            throw new IllegalArgumentException("Corrupted NBT: Number of inventory slots did not match expected.");
        }
    }

    @Override
    public void writeSyncedPersonal(CompoundNBT nbt) {
        super.writeSyncedPersonal(nbt);
        forceControllerStateData.putIntoNBT(nbt);
        nbt.put(FUEL_SLOTS_NBT, fuelZoneContents.serializeNBT());
        nbt.put(GLASS_SLOTS_NBT, glassZoneContents.serializeNBT());
        if (this.owner!=null) {
            nbt.putString(TAG_OWNER_NAME, this.owner);
        }
    }


    public void dropAllContents(World world, BlockPos blockPos) {
        InventoryHelper.dropContents(world, blockPos, fuelZoneContents);
        InventoryHelper.dropContents(world, blockPos, glassZoneContents);
    }


    /**
     *  standard code to look up what the human-readable name is.
     *  Can be useful when the tileentity has a customised name (eg "David's footlocker")
     */
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("container.aforce.force_controller");
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
        return ContainerForceController.createContainerServerSide(windowID, playerInventory,
                glassZoneContents, fuelZoneContents, forceControllerStateData);
    }

    /**
     * End of container handling
     */

    @Override
    public void onNetworkBuild(BlockPos masterPos) {
        super.onNetworkBuild(masterPos);
        this.distance = 0;
    }

    @Override
    public void handleDirty() {
        CompoundNBT shareddata = new CompoundNBT();
        this.writeSyncedShared(shareddata);
        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "DATA_SYNC");
        data.put(TAG_PACKET_MESSAGE, shareddata);

        this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_SERVANTS, data));
        this.markAsClean();
    }

    @Override
    public void onReceiveToServantsPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {}

    @Override
    public void onReceiveToMasterPacket(BlockPos myPos, int myDist, ForceNetworkPacket packet) {
        if (this.getLevel()!=null) {
            if (packet.data.getString(TAG_PACKET_TYPE).equals("DATA_SYNC")) {
                this.loadShared(this.getLevel().getBlockState(myPos), packet.data.getCompound(TAG_PACKET_MESSAGE).copy());
                this.markAsDirty();
            } else if (packet.data.getString(TAG_PACKET_TYPE).equals("CLEAR_ACTIONS")) {
                this.clearActionSelectors(TagUtil.readPos(packet.data.getCompound(TAG_PACKET_MESSAGE)));
                this.markAsDirty();
                this.markDirtyFast();
            } else if (packet.data.getString(TAG_PACKET_TYPE).equals("ADD_ACTION")) {
                this.addActionSelector(ForceModifierSelector.fromNBT(packet.data.getCompound(TAG_PACKET_MESSAGE)));
                AdvancedForcefields.LOGGER.info("Loading add_action, and marking as dirty");
                this.markAsDirty();
                this.markDirtyFast();
            }
        }
    }

    public boolean validTube(BlockState state, IBlockReader world, BlockPos pos) {
        boolean ok = state.is(AdvancedForcefieldsTags.Blocks.FORCE_COMPONENT_NO_CONTROLLER);
        //AdvancedForcefields.LOGGER.info("Checking if ["+state.getBlock().toString()+"] is a valid tube, and the answer is: "+ok);
        if (state.is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE)) {
            ok = ok && !state.getValue(ForceTubeBlock.ENABLED);
        }
        return ok; //allow network to be built through modifiers etc. too
    }

    public boolean validComponent(BlockState state, IBlockReader world, BlockPos pos) {
        boolean ok = state.is(AdvancedForcefieldsTags.Blocks.FORCE_COMPONENT_NO_CONTROLLER);
        //AdvancedForcefields.LOGGER.info("Checking if ["+state.getBlock().toString()+"] is a valid component, and the answer is: "+ok);
        if (state.is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE)) {
            ok = ok && !state.getValue(ForceTubeBlock.ENABLED);
        }
        return ok;
    }

    //time to start building our network
    public void onPowered() {
        NetworkBlockChain blockChain = new NetworkBlockChain(this.getLevel(), this.getBlockPos(),
                128, this::validTube, this::validComponent).runSearch();
        ArrayList<BlockPos> blocks = blockChain.getComponentBlocks();
        HashMap<BlockPos, Integer> distances = blockChain.getDistances();
        AdvancedForcefields.LOGGER.info("ForceController powered with a blocklist of length: "+blocks.size());
        this.onNetworkBuild(this.getBlockPos());
        for (BlockPos pos : blocks) {
            if (this.getLevel()!=null) {
                TileEntity tile = this.getLevel().getBlockEntity(pos);
                if (tile instanceof ForceNetworkTileEntity) {
                    AdvancedForcefields.LOGGER.info("ForceController adding tile: "+tile+", at pos: "+pos.toShortString());
                    ForceNetworkTileEntity networkTile = (ForceNetworkTileEntity) tile;
                    networkTile.onNetworkBuild(this.getBlockPos());
                    networkTile.setDistance(distances.get(pos));
                    networkTile.setLocked(true);
                    World world = this.getLevel();
                    BlockState state = world.getBlockState(pos);
                    if (false && state.is(AdvancedForcefieldsTags.Blocks.FORCE_TUBE)) {
                        world.setBlock(pos, state.setValue(ForceTubeBlock.ENABLED, true), Constants.BlockFlags.BLOCK_UPDATE);// | Constants.BlockFlags.NO_RERENDER);
                    }
                }
            }
        }
        this.markDirtyFast();
        this.markAsDirty();
        this.setChanged();
    }

    public void onDepowered() {
        CompoundNBT data = new CompoundNBT();
        data.putString(TAG_PACKET_TYPE, "NETWORK_RELEASE");
        data.put(TAG_PACKET_MESSAGE, new CompoundNBT());

        this.addPacket(new ForceNetworkPacket(ForceNetworkDirection.TO_SERVANTS, data, true));
        this.handlePackets();
        this.onNetworkBuild(null);
        this.setDistance(-1);
        this.setLocked(false);
    }
}