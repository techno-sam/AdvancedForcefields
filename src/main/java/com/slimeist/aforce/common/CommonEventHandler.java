package com.slimeist.aforce.common;

import com.slimeist.aforce.common.blocks.ForceControllerBlock;
import com.slimeist.aforce.core.init.BlockInit;
import com.slimeist.aforce.core.util.MiscUtil;
import com.slimeist.aforce.core.util.MobHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEventHandler {
    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        Block block = state.getBlock();
        IWorld world = event.getWorld();
        if (block.is(BlockInit.FORCE_CONTROLLER)) {
            if (!((ForceControllerBlock) block).canEntityDestroy(state, world, event.getPos(), event.getPlayer())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTargeted(final LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof PlayerEntity) {
            if (MiscUtil.isPlayerWearingFullShimmeringArmor((PlayerEntity) event.getTarget())) {
                MobHelper.resetTarget((MobEntity) event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public static void onEntityUpdate(final LivingEvent.LivingUpdateEvent event) {
        PlayerEntity player = null;

        if (event.getEntityLiving() instanceof MobEntity) {
            MobEntity mob = (MobEntity) event.getEntityLiving();
            if (mob.getTarget() instanceof PlayerEntity) {
                player = (PlayerEntity) mob.getTarget();
            } else if (mob.getLastHurtByMob() instanceof PlayerEntity) {
                player = (PlayerEntity) mob.getLastHurtByMob();
            } else if (mob instanceof IAngerable) {
                IAngerable angerableMob = (IAngerable) mob;
                if (angerableMob.getPersistentAngerTarget()!=null && mob.level.getPlayerByUUID(angerableMob.getPersistentAngerTarget()) != null) {
                    player = mob.level.getPlayerByUUID(angerableMob.getPersistentAngerTarget());
                }
            }
            if (player!=null && MiscUtil.isPlayerWearingFullShimmeringArmor(player)) {
                MobHelper.resetTarget(mob, true);
                MobHelper.resetPersistentAnger(mob);
            }
        }
    }
}
