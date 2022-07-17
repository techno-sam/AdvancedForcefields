package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.FallDamageType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;

public class BouncyAction implements IForceModifierAction {

    protected double strength;

    public BouncyAction(double strength) {
        this.strength = strength;
    }

    @Override
    public void onCollide(Level world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
        if (!this.canApplyToEntity(collider)) {return;}
        if (collisionType!=CollisionType.SOLID) {
            return;
        }
        if (interactionType == ForceInteractionType.COLLIDE || interactionType == ForceInteractionType.INSIDE || interactionType == ForceInteractionType.VERY_CLOSE) {
            Vec3 difference = collider.position().subtract(Vec3.atCenterOf(pos));

            Vec3 move = collider.getDeltaMovement();

            Vec3 new_move = new Vec3(
                    Mth.lerp(strength, difference.x(), move.x()),
                    Mth.lerp(strength, difference.y(), move.y()),
                    Mth.lerp(strength, difference.z(), move.z())
            );
            collider.setDeltaMovement(new_move);
        } else if (interactionType == ForceInteractionType.LAND_ON) {
            if (collider.getDeltaMovement().y < -0.08D) {
                SoundType soundType = SoundType.SLIME_BLOCK;
                collider.playSound(soundType.getFallSound(), (float) (soundType.getVolume() * 0.5F * Math.min(1.0d, Math.abs(collider.getDeltaMovement().y) * 2.0d)), soundType.getPitch() * 0.75F);
            }
            ((SlimeBlock) Blocks.SLIME_BLOCK).updateEntityAfterFallOn(world, collider);
        }
    }

    @Override
    public FallDamageType fallDamageType() {
        return FallDamageType.NONE;
    }
}
