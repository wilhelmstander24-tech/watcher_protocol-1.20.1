package com.mchorror.watcherprotocol;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.core.PhaseController;
import com.mchorror.watcherprotocol.core.memory.PlayerMemorySystem;
import com.mchorror.watcherprotocol.phases.phase1.MobDisruptionSystem;
import com.mchorror.watcherprotocol.registry.WatcherItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vazkii.patchouli.api.PatchouliAPI;

public class Watcher_protocol implements ModInitializer {
	public static final String MOD_ID = "watcher_protocol";


	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final PhaseController PHASE_CONTROLLER = new PhaseController();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing the watcher.");
		LOGGER.info("Initializing The Watcher Protocol.");
		WatcherConfigManager.init();
		WatcherItems.register();
		ServerTickEvents.END_WORLD_TICK.register(PHASE_CONTROLLER::tick);
		MobDisruptionSystem.register();
		PlayerMemorySystem.register();
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			handler.getPlayer().sendMessage(
					Text.translatable("watcher_protocol.init_message"),
					false
			);
		});
	}
}