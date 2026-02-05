package com.mchorror.watcherprotocol;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Watcher_protocol implements ModInitializer {
	public static final String MOD_ID = "watcher_protocol";


	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing the watcher.");
	}
}