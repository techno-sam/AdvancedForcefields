package com.slimeist.aforce.common;

import com.slimeist.aforce.common.blocks.ForceControllerBlock;
import com.slimeist.aforce.core.init.BlockInit;
import com.slimeist.aforce.core.util.MiscUtil;
import com.slimeist.aforce.core.util.MobHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonEventHandler {
    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event) {
        BlockState state = event.getState();
        Block block = state.getBlock();
        LevelAccessor world = event.getWorld();
        if (state.is(BlockInit.FORCE_CONTROLLER)) {
            if (!((ForceControllerBlock) block).canEntityDestroy(state, world, event.getPos(), event.getPlayer())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTargeted(final LivingSetAttackTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            if (MiscUtil.isPlayerWearingFullShimmeringArmor((Player) event.getTarget())) {
                MobHelper.resetTarget((Mob) event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public static void onEntityUpdate(final LivingEvent.LivingUpdateEvent event) {
        Player player = null;

        if (event.getEntityLiving() instanceof Mob) {
            Mob mob = (Mob) event.getEntityLiving();
            if (mob.getTarget() instanceof Player) {
                player = (Player) mob.getTarget();
            } else if (mob.getLastHurtByMob() instanceof Player) {
                player = (Player) mob.getLastHurtByMob();
            } else if (mob instanceof NeutralMob) {
                NeutralMob angerableMob = (NeutralMob) mob;
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
