package com.slimeist.aforce.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Calendar;

@Mixin(CreeperEntity.class)
public class MixinCreeperEntity {
    @Inject(at = @At("HEAD"), method="explodeCreeper()V", cancellable = true)
    private void explodeCreeper(CallbackInfo callback) {
        boolean april_fools;
        Calendar calendar = Calendar.getInstance();

        april_fools = calendar.get(Calendar.MONTH)==Calendar.APRIL && calendar.get(Calendar.DATE)==1;

        if (april_fools) {
            World world = ((CreeperEntity) (Object) this).level;
            CreeperEntity creeper = (CreeperEntity) (Object) this;
            if (!world.isClientSide) {
                creeper.dead = true;
                creeper.remove();
                for (Entity extra : world.getEntities(creeper, creeper.getBoundingBox().inflate(creeper.explosionRadius))) {
                    if (extra instanceof CreeperEntity) {
                        ((CreeperEntity) extra).ignite();
                    }
                }
            } else {
                Vector3d v3d = new Vector3d(0.0d, 0.0d, 0.0d);

                CompoundNBT nbt = new CompoundNBT();
                ListNBT list = new ListNBT();
                CompoundNBT explosion = new CompoundNBT();
                explosion.putByte("Type", (byte) FireworkRocketItem.Shape.CREEPER.getId());
                explosion.putBoolean("Flicker", false);
                explosion.putBoolean("Trail", false);
                explosion.putIntArray("Colors", new int[]{709140});
                explosion.putIntArray("FadeColors", new int[]{});

                list.add(explosion);
                nbt.put("Explosions", list);
                world.createFireworks(creeper.getX(), creeper.getEyeY(), creeper.getZ(), v3d.x, v3d.y, v3d.z, nbt);
            }
            callback.cancel();
        }
    }
}