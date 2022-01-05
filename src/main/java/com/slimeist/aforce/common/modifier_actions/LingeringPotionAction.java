package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.FallDamageType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlimeBlock;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

public class LingeringPotionAction implements IForceModifierAction {

    public LingeringPotionAction() {}

    @Override
    public void onCollide(World world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
        if (!this.canApplyToEntity(collider)) {return;}
        if (interactionType == ForceInteractionType.NEARBY && triggerStack.getItem() instanceof LingeringPotionItem && collider instanceof LivingEntity) {
            List<EffectInstance> effects = PotionUtils.getMobEffects(triggerStack);

            LivingEntity livingCollider = (LivingEntity) collider;

            if (effects.isEmpty() && PotionUtils.getPotion(triggerStack) == Potions.WATER) {
                if (livingCollider.isSensitiveToWater()) {
                    livingCollider.hurt(DamageSource.indirectMagic(livingCollider, null), 1.0F);
                }
            }

            for (EffectInstance effect : effects) {
                if (effect.getEffect().isInstantenous()) { //                                                         amplifier                     health
                    effect.getEffect().applyInstantenousEffect(null, null, livingCollider, effect.getAmplifier(), 0.5D);
                } else {
                    livingCollider.addEffect(new EffectInstance(effect.getEffect(), effect.getDuration() / 4, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
                }
            }
        }
    }
}
