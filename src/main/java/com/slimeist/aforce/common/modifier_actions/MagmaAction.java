package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class MagmaAction implements IForceModifierAction {

    public MagmaAction() {
    }

    @Override
    public void onCollide(Level world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
        if (!this.canApplyToEntity(collider)) {return;}
        if ((interactionType == ForceInteractionType.STEP_ON || interactionType == ForceInteractionType.INSIDE)&& collider instanceof LivingEntity) {
            LivingEntity livingCollider = (LivingEntity) collider;

            if (!livingCollider.fireImmune() && !EnchantmentHelper.hasFrostWalker(livingCollider)) {
                livingCollider.hurt(DamageSource.HOT_FLOOR, 1.0F);
            }
        }
    }
}