package com.slimeist.aforce.core.interfaces;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.FallDamageType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public interface IForceModifierAction {

    default void onCollide(IBlockReader blockReader, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType) {
    }

    default CollisionType collisionType() {
        return CollisionType.INHERIT;
    }

    default FallDamageType fallDamageType() {
        return FallDamageType.INHERIT;
    }
}
