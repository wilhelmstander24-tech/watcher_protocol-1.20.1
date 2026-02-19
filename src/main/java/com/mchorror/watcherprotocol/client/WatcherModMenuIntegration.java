package com.mchorror.watcherprotocol.client;

import com.mchorror.watcherprotocol.client.screen.WatcherConfigScreen;
import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;

public class WatcherModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			if (!WatcherConfigManager.getConfig().isAllowInGameConfig()) {
				return null;
			}
			return new WatcherConfigScreen(parent);
		};
	}
}
