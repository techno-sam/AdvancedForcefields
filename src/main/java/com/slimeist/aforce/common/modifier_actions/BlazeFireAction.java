package com.slimeist.aforce.common.modifier_actions;

import com.slimeist.aforce.core.enums.BurningType;
import com.slimeist.aforce.core.enums.CollisionType;
import com.slimeist.aforce.core.enums.ForceInteractionType;
import com.slimeist.aforce.core.interfaces.IForceModifierAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BlazeFireAction implements IForceModifierAction {

    protected float fireDamage;

    public BlazeFireAction(float fireDamage) {
        this.fireDamage = fireDamage;
    }

    @Override
    public void onCollide(Level world, BlockPos pos, Entity collider, CollisionType collisionType, ForceInteractionType interactionType, ItemStack triggerStack) {
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