package com.slimeist.aforce.core.interfaces;

import com.slimeist.aforce.core.enums.CollisionType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface IForceModifierAction {

    default void onCollide(BlockPos pos, Entity collider) {
    }

    default CollisionType collisionType() {
        return CollisionType.INHERIT;
    }
}
