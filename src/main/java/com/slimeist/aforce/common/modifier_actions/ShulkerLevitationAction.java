package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class ShulkerLevitationAction implements IForceModifierAction {

    public ShulkerLevitationAction() {
    }

    @Override
    public void onCollide(World world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
        if (interactionType == ForceInteractionType.NEARBY && collider instanceof LivingEntity && !world.isClientSide) {
            LivingEntity livingCollider = (LivingEntity) collider;

            int amplifier = triggerStack.getCount()/2;
            //200 for amplifier 0, 50 for amplifier 32
            int duration = ((32-amplifier)/32) * 150 + 50;

            livingCollider.addEffect(new EffectInstance(Effects.LEVITATION, duration, amplifier, false, true));

        }
    }
}