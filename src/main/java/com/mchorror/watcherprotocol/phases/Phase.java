package com.mchorror.watcherprotocol.phases;

import net.minecraft.server.world.ServerWorld;

public interface Phase {
    PhaseType getType();

    default void onStart(ServerWorld world) {}

    default void onStop(ServerWorld world) {}

    default void tick(ServerWorld world) {}
}
