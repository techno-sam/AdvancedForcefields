package com.slimeist.aforce.common.containers.force_controller;

import com.slimeist.aforce.common.tiles.ForceControllerTileEntity;
import com.slimeist.aforce.core.init.ContainerTypeInit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * ContainerFurnace is used to link the client side gui to the server side inventory.  It collates the various different
 * inventories into one place (using Slots)
 * It is also used to send server side data such as progress bars to the client for use in guis
 *
 * Vanilla automatically detects changes in the server side Container (the Slots and the trackedInts) and
 * sends them to the client container.
 */

/**
 * User: Slimeist
 * Date: 07/17/2021
 * Slots:
 * Fuel (Enderpearls, Eyes of Ender, etc.)
 * Glass (For determining what color the forcefield is)
 */

public class ContainerForceController extends AbstractContainerMenu {

    public static ContainerForceController createContainerServerSide(int windowID, Inventory playerInventory,
                                                                     ForceControllerZoneContents glassZoneContents,
                                                                     ForceControllerZoneContents fuelZoneContents,
                                                                     ForceControllerStateData forceControllerStateData) {
        return new ContainerForceController(windowID, playerInventory,
                glassZoneContents, fuelZoneContents, forceControllerStateData);
    }

    public static ContainerForceController createContainerClientSide(int windowID, Inventory playerInventory, net.minecraft.network.FriendlyByteBuf extraData) {
        //  don't need extraData for this example; if you want you can use it to provide extra information from the server, that you can use
        //  when creating the client container
        //  eg String detailedDescription = extraData.readString(128);
        ForceControllerZoneContents glassZoneContents = ForceControllerZoneContents.createForClientSideContainer(GLASS_SLOTS_COUNT);
        ForceControllerZoneContents fuelZoneContents = ForceControllerZoneContents.createForClientSideContainer(1);
        ForceControllerStateData forceControllerStateData = new ForceControllerStateData();

        // on the client side there is no parent TileEntity to communicate with, so we:
        // 1) use dummy inventories and furnace state data (tracked ints)
        // 2) use "do nothing" lambda functions for canPlayerAccessInventory and markDirty
        return new ContainerForceController(windowID, playerInventory,
                glassZoneContents, fuelZoneContents, forceControllerStateData);
    }

    // must assign a slot index to each of the slots used by the GUI.
    // For this container, we can see the furnace fuel, input, and output slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container using addSlotToContainer(), it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 39 = fuel slots (furnaceStateData 0 - 3)
    //  40 - 44 = input slots (furnaceStateData 4 - 8)
    //  45 - 49 = output slots (furnaceStateData 9 - 13)

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    public static final int GLASS_SLOTS_COUNT = ForceControllerTileEntity.GLASS_SLOTS_COUNT;
    public static final int FURNACE_SLOTS_COUNT = 1 + GLASS_SLOTS_COUNT;

    // slot index is the unique index for all slots in this container i.e. 0 - 35 for invPlayer then 36 - 49 for furnaceContents
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int HOTBAR_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX;
    private static final int PLAYER_INVENTORY_FIRST_SLOT_INDEX = HOTBAR_FIRST_SLOT_INDEX + HOTBAR_SLOT_COUNT;
    private static final int FIRST_FUEL_SLOT_INDEX = PLAYER_INVENTORY_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int FIRST_GLASS_SLOT_INDEX = FIRST_FUEL_SLOT_INDEX + 1;

    // gui position of the player inventory grid
    public static final int PLAYER_INVENTORY_XPOS = 8;
    public static final int PLAYER_INVENTORY_YPOS = 84;

    // slot number is the slot number within each component;
    // i.e. invPlayer slots 0 - 35 (hotbar 0 - 8 then main inventory 9 to 35)
    // and furnace: inputZone slots 0 - 4, outputZone slots 0 - 4, fuelZone 0 - 3

    public ContainerForceController(int windowID, Inventory invPlayer,
                                    ForceControllerZoneContents glassZoneContents,
                                    ForceControllerZoneContents fuelZoneContents,
                                    ForceControllerStateData forceControllerStateData) {
        super(ContainerTypeInit.FORCE_CONTROLLER_TYPE, windowID);
        if (ContainerTypeInit.FORCE_CONTROLLER_TYPE == null)
            throw new IllegalStateException("Must initialise FORCE_CONTROLLER_TYPE before constructing a ContainerForceController!");
        this.glassZoneContents = glassZoneContents;
        this.fuelZoneContents = fuelZoneContents;
        this.forceControllerStateData = forceControllerStateData;
        this.world = invPlayer.player.level;

        addDataSlots(forceControllerStateData);    // tell vanilla to keep the furnaceStateData synchronised between client and server Containers

        final int SLOT_X_SPACING = 18;
        final int SLOT_Y_SPACING = 18;
        final int HOTBAR_XPOS = 8;
        final int HOTBAR_YPOS = 142;
        // Add the players hotbar to the gui - the [xpos, ypos] location of each item
        for (int x = 0; x < HOTBAR_SLOT_COUNT; x++) {
            int slotNumber = x;
            addSlot(new Slot(invPlayer, slotNumber, HOTBAR_XPOS + SLOT_X_SPACING * x, HOTBAR_YPOS));
        }

        // Add the rest of the players inventory to the gui
        for (int y = 0; y < PLAYER_INVENTORY_ROW_COUNT; y++) {
            for (int x = 0; x < PLAYER_INVENTORY_COLUMN_COUNT; x++) {
                int slotNumber = HOTBAR_SLOT_COUNT + y * PLAYER_INVENTORY_COLUMN_COUNT + x;
                int xpos = PLAYER_INVENTORY_XPOS + x * SLOT_X_SPACING;
                int ypos = PLAYER_INVENTORY_YPOS + y * SLOT_Y_SPACING;
                addSlot(new Slot(invPlayer, slotNumber,  xpos, ypos));
            }
        }

        final int FUEL_SLOT_XPOS = 30;
        final int FUEL_SLOT_YPOS = 53;
        // Add the tile fuel slots
        addSlot(new SlotFuel(fuelZoneContents, 0, FUEL_SLOT_XPOS, FUEL_SLOT_YPOS));

        final int GLASS_SLOTS_XPOS = 98;
        final int GLASS_SLOTS_YPOS = 8;
        // Add the tile glass slots
        for (int y = 0; y < GLASS_SLOTS_COUNT; y++) {
            int slotNumber = y;
            addSlot(new SlotGlass(this.glassZoneContents, slotNumber, GLASS_SLOTS_XPOS + (SLOT_X_SPACING * (y%4)), GLASS_SLOTS_YPOS + (SLOT_Y_SPACING * ((y - (y%4))/4))));
        }
    }

    // Checks each tick to make sure the player is still able to access the inventory and if not closes the gui
    @Override
    public boolean stillValid(Player player)
    {
        return fuelZoneContents.stillValid(player) && glassZoneContents.stillValid(player);
    }

    // This is where you specify what happens when a player shift clicks a slot in the gui
    //  (when you shift click a slot in the TileEntity Inventory, it moves it to the first available position in the hotbar and/or
    //    player inventory.  When you you shift-click a hotbar or player inventory item, it moves it to the first available
    //    position in the TileEntity inventory - either input or fuel as appropriate for the item you clicked)
    // At the very least you must override this and return ItemStack.EMPTY or the game will crash when the player shift clicks a slot.
    // returns ItemStack.EMPTY if the source slot is empty, or if none of the source slot item could be moved.
    //   otherwise, returns a copy of the source stack
    //  Code copied & refactored from vanilla furnace AbstractFurnaceContainer
    @Override
    public ItemStack quickMoveStack(Player player, int sourceSlotIndex)
    {
        Slot sourceSlot = slots.get(sourceSlotIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceItemStack = sourceSlot.getItem();
        ItemStack sourceStackBeforeMerge = sourceItemStack.copy();
        boolean successfulTransfer = false;

        SlotZone sourceZone = SlotZone.getZoneFromIndex(sourceSlotIndex);

        switch (sourceZone) {
            case GLASS_ZONE: // taking out of the output zone - try the hotbar first, then main inventory.  fill from the end.
                successfulTransfer = mergeInto(SlotZone.PLAYER_HOTBAR, sourceItemStack, true);
                if (!successfulTransfer) {
                    successfulTransfer = mergeInto(SlotZone.PLAYER_MAIN_INVENTORY, sourceItemStack, true);
                }
                if (successfulTransfer) {  // removing from output means we have just crafted an item -> need to inform
                    sourceSlot.onQuickCraft(sourceItemStack, sourceStackBeforeMerge);
                }
                break;

            case FUEL_ZONE: // taking out of input zone or fuel zone - try player main inv first, then hotbar.  fill from the start
                successfulTransfer = mergeInto(SlotZone.PLAYER_MAIN_INVENTORY, sourceItemStack, false);
                if (!successfulTransfer) {
                    successfulTransfer = mergeInto(SlotZone.PLAYER_HOTBAR, sourceItemStack, false);
                }
                break;

            case PLAYER_HOTBAR:
            case PLAYER_MAIN_INVENTORY: // taking out of inventory - find the appropriate furnace zone
                if (ForceControllerTileEntity.isItemValidForGlassSlot(sourceItemStack)) { // is glass -> add to glass
                    successfulTransfer = mergeInto(SlotZone.GLASS_ZONE, sourceItemStack, false);
                }
                if (!successfulTransfer && ForceControllerTileEntity.getItemBurnTime(world, sourceItemStack) > 0) { //burnable -> add to fuel from the bottom slot first
                    successfulTransfer = mergeInto(SlotZone.FUEL_ZONE, sourceItemStack, true);
                }
                if (!successfulTransfer) {  // didn't fit into furnace; try player main inventory or hotbar
                    if (sourceZone == SlotZone.PLAYER_HOTBAR) { // main inventory
                        successfulTransfer = mergeInto(SlotZone.PLAYER_MAIN_INVENTORY, sourceItemStack, false);
                    } else {
                        successfulTransfer = mergeInto(SlotZone.PLAYER_HOTBAR, sourceItemStack, false);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("unexpected sourceZone:" + sourceZone);
        }
        if (!successfulTransfer) return ItemStack.EMPTY;

        // If source stack is empty (the entire stack was moved) set slot contents to empty
        if (sourceItemStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        // if source stack is still the same as before the merge, the transfer failed somehow?  not expected.
        if (sourceItemStack.getCount() == sourceStackBeforeMerge.getCount()) {
            return ItemStack.EMPTY;
        }
        sourceSlot.onTake(player, sourceItemStack);
        return sourceStackBeforeMerge;
    }

    /**
     * Try to merge from the given source ItemStack into the given SlotZone.
     * @param destinationZone the zone to merge into
     * @param sourceItemStack the itemstack to merge from
     * @param fillFromEnd if true: try to merge from the end of the zone instead of from the start
     * @return true if a successful transfer occurred
     */
    private boolean mergeInto(SlotZone destinationZone, ItemStack sourceItemStack, boolean fillFromEnd) {
        return moveItemStackTo(sourceItemStack, destinationZone.firstIndex, destinationZone.lastIndexPlus1, fillFromEnd);
    }

    // -------- methods used by the ContainerScreen to render parts of the display

    /**
     * Returns the amount of fuel remaining on the currently burning item in the given fuel slot.
     * @return fraction remaining, between 0.0 - 1.0
     */
    public double fractionOfFuelRemaining() {
        if (forceControllerStateData.burnTimeInitialValue <= 0 ) return 0;
        double fraction = forceControllerStateData.burnTimeRemaining / (double) forceControllerStateData.burnTimeInitialValue;
        return Mth.clamp(fraction, 0.0, 1.0);
    }

    /**
     * return the remaining burn time of the fuel in the given slot
     * @return seconds remaining
     */
    public int secondsOfFuelRemaining()	{
        if (forceControllerStateData.burnTimeRemaining <= 0 ) return 0;
        return forceControllerStateData.burnTimeRemaining / 20; // 20 ticks per second
    }

    // --------- Customise the different slots (in particular - what items they will accept)


    // SlotFuel is a slot for fuel items
    public class SlotFuel extends Slot {
        public SlotFuel(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        // if this function returns false, the player won't be able to insert the given item into this slot
        @Override
        public boolean mayPlace(ItemStack stack) {
            return ForceControllerTileEntity.isItemValidForFuelSlot(stack);
        }
    }

    // SlotGlass is a slot that will only accept glass
    public class SlotGlass extends Slot {
        public SlotGlass(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        // if this function returns false, the player won't be able to insert the given item into this slot
        @Override
        public boolean mayPlace(ItemStack stack) {
            return ForceControllerTileEntity.isItemValidForGlassSlot(stack);
        }
    }

    private ForceControllerZoneContents glassZoneContents;
    private ForceControllerZoneContents fuelZoneContents;
    private ForceControllerStateData forceControllerStateData;

    private Level world; //needed for some helper methods
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Helper enum to make the code more readable
     */
    private enum SlotZone {
        FUEL_ZONE(FIRST_FUEL_SLOT_INDEX, 1),
        GLASS_ZONE(FIRST_GLASS_SLOT_INDEX, GLASS_SLOTS_COUNT),
        PLAYER_MAIN_INVENTORY(PLAYER_INVENTORY_FIRST_SLOT_INDEX, PLAYER_INVENTORY_SLOT_COUNT),
        PLAYER_HOTBAR(HOTBAR_FIRST_SLOT_INDEX, HOTBAR_SLOT_COUNT);

        SlotZone(int firstIndex, int numberOfSlots) {
            this.firstIndex = firstIndex;
            this.slotCount = numberOfSlots;
            this.lastIndexPlus1 = firstIndex + numberOfSlots;
        }

        public final int firstIndex;
        public final int slotCount;
        public final int lastIndexPlus1;

        public static SlotZone getZoneFromIndex(int slotIndex) {
            for (SlotZone slotZone : SlotZone.values()) {
                if (slotIndex >= slotZone.firstIndex && slotIndex < slotZone.lastIndexPlus1) return slotZone;
            }
            throw new IndexOutOfBoundsException("Unexpected slotIndex");
        }
    }
}