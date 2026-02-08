package com.mchorror.watcherprotocol.phases.phase3;

import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import net.minecraft.server.world.ServerWorld;

public class ObserverPhase implements Phase {
    @Override
    public PhaseType getType() {
        return PhaseType.PHASE_3;
    }

    @Override
    public void onStart(ServerWorld world) {
        // Phase 3 server-side effects are currently handled by client perceptual systems.
    }
}