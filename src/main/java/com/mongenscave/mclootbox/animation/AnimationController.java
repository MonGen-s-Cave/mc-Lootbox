package com.mongenscave.mclootbox.animation;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnimationController {

    private static final Set<UUID> ACTIVE = ConcurrentHashMap.newKeySet();

    private AnimationController() {
    }

    public static boolean tryStart(UUID uuid) {
        return ACTIVE.add(uuid);
    }

    public static void end(UUID uuid) {
        ACTIVE.remove(uuid);
    }
}