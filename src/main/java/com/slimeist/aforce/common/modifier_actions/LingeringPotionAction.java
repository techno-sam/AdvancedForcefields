package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

import java.util.List;

public class LingeringPotionAction implements IForceModifierAction {

    public LingeringPotionAction() {}

    @Override
    public void onCollide(Level world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
        if (!this.canApplyToEntity(collider)) {return;}
        if (interactionType == ForceInteractionType.NEARBY && triggerStack.getItem() instanceof LingeringPotionItem && collider instanceof LivingEntity) {
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(triggerStack);

            LivingEntity livingCollider = (LivingEntity) collider;

            if (effects.isEmpty() && PotionUtils.getPotion(triggerStack) == Potions.WATER) {
                if (livingCollider.isSensitiveToWater()) {
                    livingCollider.hurt(DamageSource.indirectMagic(livingCollider, null), 1.0F);
                }
            }

            for (MobEffectInstance effect : effects) {
                if (effect.getEffect().isInstantenous()) { //                                                         amplifier                     health
                    effect.getEffect().applyInstantenousEffect(null, null, livingCollider, effect.getAmplifier(), 0.5D);
                } else {
                    livingCollider.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration() / 4, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
                }
            }
        }
    }
}
