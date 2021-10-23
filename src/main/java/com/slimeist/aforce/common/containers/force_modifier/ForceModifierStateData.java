package com.slimeist.aforce.common.containers.force_modifier;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IIntArray;

public class ForceModifierStateData implements IIntArray {

    /** example */
    public int example;
    public static final String EXAMPLE_TAG = "example";

    // --------- read/write to NBT for permanent storage (on disk, or packet transmission) - used by the TileEntity only

    public void putIntoNBT(CompoundNBT nbtTagCompound) {
        nbtTagCompound.putInt(EXAMPLE_TAG, example);
    }

    public void readFromNBT(CompoundNBT nbtTagCompound) {
        // Trim the arrays (or pad with 0) to make sure they have the correct number of elements
        example = nbtTagCompound.getInt(EXAMPLE_TAG);
    }

    // -------- used by vanilla, not intended for mod code
//  * The ints are mapped (internally) as:
//  * 0 = cookTimeElapsed
//  * 1 = cookTimeForCompletion
//  * 2 .. FUEL_SLOTS_COUNT+1 = burnTimeInitialValues[]
//  * FUEL_SLOTS_COUNT + 2 .. 2*FUEL_SLOTS_COUNT +1 = burnTimeRemainings[]
//  *

    private final int EXAMPLE_INDEX = 0;
    private final int END_OF_DATA_INDEX_PLUS_ONE = EXAMPLE_INDEX + 1;

    @Override
    public int get(int index) {
        validateIndex(index);
        if (index == EXAMPLE_INDEX) {
            return example;
        } else {
            return example;
        }
    }

    @Override
    public void set(int index, int value) {
        validateIndex(index);
        if (index == EXAMPLE_INDEX) {
            example = value;
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