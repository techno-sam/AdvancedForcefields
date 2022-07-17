package com.slimeist.aforce.core.interfaces;

import com.slimeist.aforce.core.enums.BurningType;
import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.FallDamageType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IForceModifierAction {

    default void onCollide(Level world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
    }

    default CollisionType collisionType() {
        return CollisionType.INHERIT;
    }

    default FallDamageType fallDamageType() {
        return FallDamageType.INHERIT;
    }

    default BurningType burningType() {
        return BurningType.INHERIT;
    }

    default boolean canApplyToEntity(Entity entity) {
        return !(entity instanceof Player) || !((Player) entity).isSpectator();
    }
}
