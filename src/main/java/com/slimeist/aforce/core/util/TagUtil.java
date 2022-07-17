package com.slimeist.aforce.core.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class TagUtil {
    //copied from Tinker's Construct
    /**
     * Writes a block position to NBT
     * @param pos  Position to write
     * @return  Position in NBT
     */
    public static CompoundTag writePos(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    /**
     * Reads a block position from NBT
     * @param tag  Tag
     * @return  Block position, or null if invalid
     */
    @Nullable
    public static BlockPos readPos(CompoundTag tag) {
        if (tag.contains("x", Tag.TAG_ANY_NUMERIC) && tag.contains("y", Tag.TAG_ANY_NUMERIC) && tag.contains("z", Tag.TAG_ANY_NUMERIC)) {
            return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }
        return null;
    }

    /**
     * Reads a block position from NBT
     * @param parent  Parent tag
     * @param key     Position key
     * @return  Block position, or null if invalid or missing
     */
    @Nullable
    public static BlockPos readPos(CompoundTag parent, String key) {
        if (parent.contains(key, Tag.TAG_COMPOUND)) {
            return readPos(parent.getCompound(key));
        }
        return null;
    }
}
