package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class BouncyAction implements IForceModifierAction {

    protected double strength;

    public BouncyAction(double strength) {
        this.strength = strength;
    }

    @Override
    public void onCollide(BlockPos pos, Entity collider) {
        Vector3d difference = collider.position().subtract(Vector3d.atCenterOf(pos));

        Vector3d move = collider.getDeltaMovement();

        Vector3d new_move = new Vector3d(
                MathHelper.lerp(strength, difference.x(), move.x()),
                MathHelper.lerp(strength, difference.y(), move.y()),
                MathHelper.lerp(strength, difference.z(), move.z())
        );
        collider.setDeltaMovement(new_move);
    }
}
