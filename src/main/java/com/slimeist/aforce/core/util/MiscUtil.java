package com.slimeist.aforce.core.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.slimeist.aforce.common.AdvancedForcefieldsTags;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class MiscUtil {

    public static void syncTE(TileEntity tile) {
        SUpdateTileEntityPacket packet = tile.getUpdatePacket();
        if (packet != null && tile.getLevel() instanceof ServerWorld) {
            ((ServerChunkProvider) tile.getLevel().getChunkSource()).chunkMap
                    .getPlayers(new ChunkPos(tile.getBlockPos()), false)
                    .forEach(e -> e.connection.send(packet));
        }
    }

    public static boolean isPlayerWearingFullShimmeringArmor(PlayerEntity player) {
        return player.getItemBySlot(EquipmentSlotType.HEAD).getItem().is(AdvancedForcefieldsTags.Items.SHIMMERING_HELMET) &&
                player.getItemBySlot(EquipmentSlotType.CHEST).getItem().is(AdvancedForcefieldsTags.Items.SHIMMERING_CHESTPLATE) &&
                player.getItemBySlot(EquipmentSlotType.LEGS).getItem().is(AdvancedForcefieldsTags.Items.SHIMMERING_LEGGINGS) &&
                player.getItemBySlot(EquipmentSlotType.FEET).getItem().is(AdvancedForcefieldsTags.Items.SHIMMERING_BOOTS);
    }

    public static boolean isPlayerWearingShimmeringHelmet(PlayerEntity player) {
        return player.getItemBySlot(EquipmentSlotType.HEAD).getItem().is(AdvancedForcefieldsTags.Items.SHIMMERING_HELMET);
    }

    public static double randomDouble(double lowerBound, double upperBound, Random rand) {
        double zero_to_1 = rand.nextDouble();
        double zero_to_scaled = zero_to_1 * (upperBound - lowerBound);
        return zero_to_scaled + lowerBound;
    }

    public static double randomSignedDouble(double lowerBound, double upperBound, Random rand) {
        return randomDouble(lowerBound, upperBound, rand) * (rand.nextBoolean() ? -1 : 1);
    }

    public static Object randomChoice(ArrayList<?> list, Random rand) {
        int index = rand.nextInt(list.size());
        return list.get(index);
    }

    /**
     * 0.0d percent is a.
     * 1.0d percent is b.
     * 0.5d percent is evenly mixed, etc..
     */
    public static double lerp(double a, double b, double percent) {
        percent = Math.min(1.0d, Math.max(0.0d, percent));
        return (b * percent) + (a * (1.0d - percent));
    }

    public static Optional<Predicate<Entity>> predicateFromSelector(String selector) {
        if (selector.isEmpty() || selector.charAt(0) != '@') {
            return Optional.empty();
        }
        EntitySelectorParser esp = new EntitySelectorParser(new StringReader(selector));
        try {
            EntitySelector es = esp.parse();
            if (es.isSelfSelector()) {
                return Optional.empty();
            }
            if (esp.getDeltaX() != null || esp.getDeltaY() != null || esp.getDeltaZ() != null || esp.getDistance() != MinMaxBounds.FloatBound.ANY) {
                return Optional.empty();
            }
            Predicate<Entity> entityPredicate = es.getPredicate(Vector3d.ZERO);
            return Optional.of(entityPredicate);
        } catch (CommandSyntaxException e) {
            return Optional.empty();
        }
    }

    public static Optional<CompletableFuture<Suggestions>> suggestionsFromSelector(String selector, int start) {
        if (selector.isEmpty() || selector.charAt(0) != '@') {
            return Optional.of(Suggestions.empty());
        }
        if (selector.length() >= 2 && selector.charAt(1) == 's') {
            return Optional.of(Suggestions.empty());
        }
        EntitySelectorParser esp = new EntitySelectorParser(new StringReader(selector));

        try {
            esp.parse();
        } catch (CommandSyntaxException ignored) {}

        SuggestionsBuilder builder = new SuggestionsBuilder(selector, start);
        return Optional.of(esp.fillSuggestions(builder, (a) -> {}));
    }
}
