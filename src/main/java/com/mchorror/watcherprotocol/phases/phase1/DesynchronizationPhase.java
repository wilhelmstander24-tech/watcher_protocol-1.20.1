package com.mchorror.watcherprotocol.phases.phase1;

import com.mchorror.watcherprotocol.Watcher_protocol;
import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import net.minecraft.server.world.ServerWorld;

public class DesynchronizationPhase implements Phase {
    private static final int LOG_INTERVAL_TICKS = 2400;

    private int logCooldown = LOG_INTERVAL_TICKS;

    @Override
    public PhaseType getType() {
        return PhaseType.PHASE_1;
    }

    @Override
    public void onStart(ServerWorld world) {
        logCooldown = LOG_INTERVAL_TICKS;
        Watcher_protocol.LOGGER.info("Phase 1 started for world {}.", world.getRegistryKey().getValue());
    }

    @Override
    public void onStop(ServerWorld world) {
        Watcher_protocol.LOGGER.info("Phase 1 stopped for world {}.", world.getRegistryKey().getValue());
    }

    @Override
    public void tick(ServerWorld world) {
        if (--logCooldown > 0) {
            return;
        }

        logCooldown = LOG_INTERVAL_TICKS;
        Watcher_protocol.LOGGER.debug("Phase 1 heartbeat for world {}.", world.getRegistryKey().getValue());
    }
}