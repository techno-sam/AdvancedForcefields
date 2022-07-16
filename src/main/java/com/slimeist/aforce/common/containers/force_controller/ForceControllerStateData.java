package com.slimeist.aforce.common.containers.force_controller;

import com.slimeist.aforce.common.tiles.ForceControllerTileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerData;

public class ForceControllerStateData implements ContainerData {

    /**The color of our forcefield*/
    public int color;

    /** The initial fuel value of the currently burning fuel in each slot (in ticks of burn duration) */
    public int burnTimeInitialValue;
    /** The number of burn ticks remaining on the current piece of fuel in each slot */
    public int burnTimeRemaining;

    // --------- read/write to NBT for permanent storage (on disk, or packet transmission) - used by the TileEntity only

    public void putIntoNBT(CompoundTag nbtTagCompound) {
        //nbtTagCompound.putInt("color", color);
        nbtTagCompound.putInt("burnTimeRemaining", burnTimeRemaining);
        nbtTagCompound.putInt("burnTimeInitial", burnTimeInitialValue);
    }

    public void readFromNBT(CompoundTag nbtTagCompound) {
        // Trim the arrays (or pad with 0) to make sure they have the correct number of elements
        //color = nbtTagCompound.getInt("color");
        burnTimeRemaining = nbtTagCompound.getInt("burnTimeRemaining");
        burnTimeInitialValue = nbtTagCompound.getInt("burnTimeInitialValue");
    }

    // -------- used by vanilla, not intended for mod code
//  * The ints are mapped (internally) as:
//  * 0 = cookTimeElapsed
//  * 1 = cookTimeForCompletion
//  * 2 .. FUEL_SLOTS_COUNT+1 = burnTimeInitialValues[]
//  * FUEL_SLOTS_COUNT + 2 .. 2*FUEL_SLOTS_COUNT +1 = burnTimeRemainings[]
//  *

    private final int COLOR_INDEX = 0;
    private final int BURNTIME_INITIAL_VALUE_INDEX = 1;
    private final int BURNTIME_REMAINING_INDEX = BURNTIME_INITIAL_VALUE_INDEX + 1;
    private final int END_OF_DATA_INDEX_PLUS_ONE = BURNTIME_REMAINING_INDEX + 1;

    @Override
    public int get(int index) {
        validateIndex(index);
        if (index == COLOR_INDEX) {
            return color;
        } else if (index >= BURNTIME_INITIAL_VALUE_INDEX && index < BURNTIME_REMAINING_INDEX) {
            return burnTimeInitialValue;
        } else {
            return burnTimeRemaining;
        }
    }

    @Override
    public void set(int index, int value) {
        validateIndex(index);
        if (index == COLOR_INDEX) {
            color = value;
        } else if (index >= BURNTIME_INITIAL_VALUE_INDEX && index < BURNTIME_REMAINING_INDEX) {
            burnTimeInitialValue = value;
        } else {
            burnTimeRemaining = value;
        }
    }

    @Override
    public int getCount() {
        return END_OF_DATA_INDEX_PLUS_ONE;
    }

    private void validateIndex(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("Index out of bounds:"+index);
        }
    }
}