package com.slimeist.aforce.core.util;

import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;

//This class is copied from P3pp3rF1y's Reliquary mod

public class MobHelper {
    private MobHelper() {}

    public static void resetTarget(Mob entity) {
        resetTarget(entity, false);
    }

    public static void resetTarget(Mob entity, boolean resetRevengeTarget) {
        Brain<?> brain = entity.getBrain();
        brain.setMemory(MemoryModuleType.ATTACK_TARGET, Optional.empty());
        brain.setMemory(MemoryModuleType.ANGRY_AT, Optional.empty());
        brain.setMemory(MemoryModuleType.UNIVERSAL_ANGER, Optional.empty());
        entity.setTarget(null);
        if (resetRevengeTarget) {
            entity.setLastHurtByMob(null);
        }
    }

    //following method added by Slimeist
    public static void resetPersistentAnger(Mob entity) {
        if (entity instanceof NeutralMob) {
            ((NeutralMob) entity).stopBeingAngry();
        }
    }
}
