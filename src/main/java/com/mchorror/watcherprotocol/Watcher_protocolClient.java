package com.mchorror.watcherprotocol;

import com.mchorror.watcherprotocol.phases.phase3.PhaseThreeClientEffects;
import net.fabricmc.api.ClientModInitializer;

public class Watcher_protocolClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PhaseThreeClientEffects.register();
    }
}