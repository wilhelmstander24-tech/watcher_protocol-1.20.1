package com.mchorror.watcherprotocol.phases.phase6.system;

import net.minecraft.server.world.ServerWorld;

public interface PhaseSixSystem {
    default void onStart(ServerWorld world, double corruptionLevel) {}

    default void onStop(ServerWorld world) {}

    void tick(ServerWorld world, double corruptionLevel);
}
