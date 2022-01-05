package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.AdvancedForcefields;
import com.slimeist.aforce.core.enums.BurningType;
import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlazeFireAction implements IForceModifierAction {

    protected float fireDamage;

    public BlazeFireAction(float fireDamage) {
        this.fireDamage = fireDamage;
    }

    @Override
    public void onCollide(World world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
        if (!this.canApplyToEntity(collider)) {return;}
        if (interactionType == ForceInteractionType.VERY_CLOSE) {
            if (!collider.fireImmune()) {
                collider.setRemainingFireTicks(collider.getRemainingFireTicks() + 2);
                if (collider.getRemainingFireTicks() == 0) {
                    collider.setSecondsOnFire(8);
                }
                collider.hurt(DamageSource.IN_FIRE, this.fireDamage);
            }
        }
    }

    @Override
    public BurningType burningType() {
        return BurningType.BURN;
    }
}