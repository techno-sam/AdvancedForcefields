package com.slimeist.aforce.core.interfaces;

import com.slimeist.aforce.core.enums.BurningType;
import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.FallDamageType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public interface IForceModifierAction {

    default void onCollide(World world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
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
        return !(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isSpectator();
    }
}
