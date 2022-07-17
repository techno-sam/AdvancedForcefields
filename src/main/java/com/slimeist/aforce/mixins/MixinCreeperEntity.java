package com.slimeist.aforce.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Calendar;

@Mixin(Creeper.class)
public class MixinCreeperEntity {
    @Inject(at = @At("HEAD"), method="explodeCreeper()V", cancellable = true)
    private void explodeCreeper(CallbackInfo callback) {
        boolean april_fools;
        Calendar calendar = Calendar.getInstance();

        april_fools = calendar.get(Calendar.MONTH)==Calendar.APRIL && calendar.get(Calendar.DATE)==1;

        if (april_fools) {
            Level world = ((Creeper) (Object) this).level;
            Creeper creeper = (Creeper) (Object) this;
            if (!world.isClientSide) {
                creeper.dead = true;
                creeper.discard();
                for (Entity extra : world.getEntities(creeper, creeper.getBoundingBox().inflate(creeper.explosionRadius))) {
                    if (extra instanceof Creeper) {
                        ((Creeper) extra).ignite();
                    }
                }
            } else {
                Vec3 v3d = new Vec3(0.0d, 0.0d, 0.0d);

                CompoundTag nbt = new CompoundTag();
                ListTag list = new ListTag();
                CompoundTag explosion = new CompoundTag();
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