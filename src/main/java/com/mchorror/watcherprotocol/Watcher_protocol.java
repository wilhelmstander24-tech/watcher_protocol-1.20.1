package com.mchorror.watcherprotocol;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.core.PhaseController;
import com.mchorror.watcherprotocol.core.memory.PlayerMemorySystem;
import com.mchorror.watcherprotocol.phases.phase1.MobDisruptionSystem;
import com.mchorror.watcherprotocol.registry.WatcherItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Watcher_protocol implements ModInitializer {
	public static final String MOD_ID = "watcher_protocol";
	private static final Identifier GUIDE_BOOK_ID = new Identifier(MOD_ID, "watcher_survival_guide");
	private static final String GUIDE_TAG = MOD_ID + ".guide_received";


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
			var player = handler.getPlayer();
			player.sendMessage(Text.translatable("watcher_protocol.init_message"), false);

			if (player.getCommandTags().contains(GUIDE_TAG)) {
				return;
			}

			ItemStack guideBook = createSurvivalManual();
			if (!player.getInventory().insertStack(guideBook.copy())) {
				player.dropItem(guideBook.copy(), false);
			}
			player.addCommandTag(GUIDE_TAG);
		});
	}
	private static ItemStack createSurvivalManual() {
		ItemStack manual = new ItemStack(Items.WRITTEN_BOOK);
		NbtCompound nbt = manual.getOrCreateNbt();
		nbt.putString("title", "Watcher Survival Manual");
		nbt.putString("author", "Protocol Archive");
		NbtList pages = new NbtList();
		pages.add(NbtString.of(jsonPage("§lWATCHER SURVIVAL MANUAL§r\n\nYou are not alone in this world anymore. This manual explains escalation phases, danger signs, and survival doctrine.")));
		pages.add(NbtString.of(jsonPage("§lPhase 1-2: Warning§r\n\n• Irregular crops/furnaces\n• False footsteps/mining\n\nAction: Build redundancy. Record anomalies. Do not assume the world is stable.")));
		pages.add(NbtString.of(jsonPage("§lPhase 3-4: Presence§r\n\n• Visual distortions\n• Doors/terrain interference\n\nAction: Keep fallback shelter and off-site supply cache. Travel with clear retreat routes.")));
		pages.add(NbtString.of(jsonPage("§lPhase 5-7: Collapse§r\n\n• Behavioral mimicry\n• Manifestation risk\n• Reality faults\n\nAction: Avoid routines. Preserve mobility. Abandon world if infrastructure fails.")));
		pages.add(NbtString.of(jsonPage("§lField Rules§r\n\n1) Never investigate every sound.\n2) Mark all exploration branches.\n3) Keep emergency gear cached.\n4) Survival outranks curiosity.")));
		nbt.put("pages", pages);
		return manual;
	}

	private static String jsonPage(String text) {
		String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
		return "{\"text\":\"" + escaped + "\"}";
	}
}