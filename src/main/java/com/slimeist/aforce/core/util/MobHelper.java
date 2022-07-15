package com.slimeist.aforce.core.util;

import net.minecraft.entity.IAngerable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;

import java.util.Optional;

//This class is copied from P3pp3rF1y's Reliquary mod

public class MobHelper {
    private MobHelper() {}

    public static void resetTarget(MobEntity entity) {
        resetTarget(entity, false);
    }

    public static void resetTarget(MobEntity entity, boolean resetRevengeTarget) {
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
    public static void resetPersistentAnger(MobEntity entity) {
        if (entity instanceof IAngerable) {
            ((IAngerable) entity).stopBeingAngry();
        }
    }
}
