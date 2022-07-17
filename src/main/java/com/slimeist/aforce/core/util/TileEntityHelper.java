package com.slimeist.aforce.core.util;

import com.slimeist.aforce.AdvancedForcefields;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileEntityHelper {
    //Copied from Tinker's Construct
    /**
     * Gets a tile entity if present and the right type
     * @param clazz  Tile entity class
     * @param world  World instance
     * @param pos    Tile entity position
     * @param <T>    Tile entity type
     * @return  Optional of the tile entity, empty if missing or wrong class
     */
    public static <T> Optional<T> getTile(Class<T> clazz, @Nullable BlockGetter world, BlockPos pos) {
        return getTile(clazz, world, pos, false);
    }

    /**
     * Gets a tile entity if present and the right type
     * @param clazz         Tile entity class
     * @param world         World instance
     * @param pos           Tile entity position
     * @param logWrongType  If true, logs a warning if the type is wrong
     * @param <T>    Tile entity type
     * @return  Optional of the tile entity, empty if missing or wrong class
     */
    public static <T> Optional<T>  getTile(Class<T> clazz, @Nullable BlockGetter world, BlockPos pos, boolean logWrongType) {
        if (!isBlockLoaded(world, pos)) {
            return Optional.empty();
        }

        //TODO: This causes freezes if being called from onLoad
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile == null) {
            return Optional.empty();
        }

        if (clazz.isInstance(tile)) {
            return Optional.of(clazz.cast(tile));
        } else if (logWrongType) {
            AdvancedForcefields.LOGGER.warn("Unexpected TileEntity class at {}, expected {}, but found: {}", pos, clazz, tile.getClass());
        }

        return Optional.empty();
    }

    /**
     * Checks if the given block is loaded
     * @param world  World instance
     * @param pos    Position to check
     * @return  True if its loaded
     */
    public static boolean isBlockLoaded(@Nullable BlockGetter world, BlockPos pos) {
        if (world == null) {
            return false;
        }
        if (world instanceof LevelReader) {
            return ((LevelReader) world).hasChunkAt(pos);
        }
        return true;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> type, BlockEntityType<E> expected, BlockEntityTicker<? super E> tick_method) {
        return expected == type ? (BlockEntityTicker<A>)tick_method : null;
    }
}