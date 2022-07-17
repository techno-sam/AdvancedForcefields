package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShulkerLevitationAction implements IForceModifierAction {

    public ShulkerLevitationAction() {
    }

    @Override
    public void onCollide(Level world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
        if (!this.canApplyToEntity(collider)) {return;}
        if (interactionType == ForceInteractionType.NEARBY && collider instanceof LivingEntity && !world.isClientSide) {
            LivingEntity livingCollider = (LivingEntity) collider;

            int amplifier = triggerStack.getCount()/2;
            //200 for amplifier 0, 50 for amplifier 32
            int duration = ((32-amplifier)/32) * 150 + 50;

            livingCollider.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, amplifier, false, true));

        }
    }
}