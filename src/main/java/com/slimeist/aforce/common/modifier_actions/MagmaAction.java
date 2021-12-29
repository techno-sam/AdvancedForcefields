package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MagmaAction implements IForceModifierAction {

    public MagmaAction() {
    }

    @Override
    public void onCollide(World world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
        if ((interactionType == ForceInteractionType.STEP_ON || interactionType == ForceInteractionType.INSIDE)&& collider instanceof LivingEntity) {
            LivingEntity livingCollider = (LivingEntity) collider;

            if (!livingCollider.fireImmune() && !EnchantmentHelper.hasFrostWalker(livingCollider)) {
                livingCollider.hurt(DamageSource.HOT_FLOOR, 1.0F);
            }
        }
    }
}