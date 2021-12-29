package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.FallDamageType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class BouncyAction implements IForceModifierAction {

    protected double strength;

    public BouncyAction(double strength) {
        this.strength = strength;
    }

    @Override
    public void onCollide(IBlockReader blockReader, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
        if (collisionType!=CollisionType.SOLID) {
            return;
        }
        if (interactionType == ForceInteractionType.COLLIDE || interactionType == ForceInteractionType.INSIDE || interactionType == ForceInteractionType.VERY_CLOSE) {
            Vector3d difference = collider.position().subtract(Vector3d.atCenterOf(pos));

            Vector3d move = collider.getDeltaMovement();

            Vector3d new_move = new Vector3d(
                    MathHelper.lerp(strength, difference.x(), move.x()),
                    MathHelper.lerp(strength, difference.y(), move.y()),
                    MathHelper.lerp(strength, difference.z(), move.z())
            );
            collider.setDeltaMovement(new_move);
        } else if (interactionType == ForceInteractionType.LAND_ON) {
            if (collider.getDeltaMovement().y < -0.08D) {
                SoundType soundType = SoundType.SLIME_BLOCK;
                collider.playSound(soundType.getFallSound(), (float) (soundType.getVolume() * 0.5F * Math.min(1.0d, Math.abs(collider.getDeltaMovement().y) * 2.0d)), soundType.getPitch() * 0.75F);
            }
            ((SlimeBlock) Blocks.SLIME_BLOCK).updateEntityAfterFallOn(blockReader, collider);
        }
    }

    @Override
    public FallDamageType fallDamageType() {
        return FallDamageType.NONE;
    }
}
