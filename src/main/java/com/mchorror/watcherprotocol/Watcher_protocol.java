package com.mchorror.watcherprotocol;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.core.PhaseController;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Watcher_protocol implements ModInitializer {
	public static final String MOD_ID = "watcher_protocol";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final PhaseController PHASE_CONTROLLER = new PhaseController();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing The Watcher Protocol.");
		WatcherConfigManager.init();
		ServerTickEvents.END_WORLD_TICK.register(PHASE_CONTROLLER::tick);
	}
}