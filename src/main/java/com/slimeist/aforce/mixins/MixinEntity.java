package com.slimeist.aforce.mixins;

import com.slimeist.aforce.common.blocks.ForceTubeBlock;
import com.slimeist.aforce.common.tiles.ForceTubeTileEntity;
import com.slimeist.aforce.core.enums.BurningType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {
    @Shadow
    public AABB getBoundingBox() {
        throw new RuntimeException("getBoundingBox() should have been mixed in");
    }

    @Shadow
    public Level level;

    @Shadow
    private int remainingFireTicks;

    @Shadow
    public void setRemainingFireTicks(int ticks) {
        throw new RuntimeException("setRemainingFireTicks() should have been mixed in");}

    @Shadow
    protected int getFireImmuneTicks() {
        throw new RuntimeException("getFireImmuneTicks() should have been mixed in");
    }

    @Inject(at = @At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;setRemainingFireTicks(I)V", ordinal=0), method="move", cancellable = true)
    private void modifyTicks(CallbackInfo callback) {
        if (BlockPos.betweenClosedStream(this.getBoundingBox().deflate(0.001D)).noneMatch((p_233572_0_) -> {
            BlockState state = level.getBlockState(p_233572_0_);
            if (state.getBlock() instanceof ForceTubeBlock) {
                BlockEntity tile = level.getBlockEntity(p_233572_0_);
                if (tile instanceof ForceTubeTileEntity) {
                    return ((ForceTubeBlock) state.getBlock()).getBurningType((Entity) (Object) this, (ForceTubeTileEntity) tile) == BurningType.BURN;
                }
            }
            return false;
        })) {
            this.setRemainingFireTicks(-this.getFireImmuneTicks());
        }
        callback.cancel();
    }
}