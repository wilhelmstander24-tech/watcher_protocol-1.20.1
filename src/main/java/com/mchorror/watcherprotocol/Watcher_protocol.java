package com.mchorror.watcherprotocol;

import net.fabricmc.api.ModInitializer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.core.PhaseController;
import com.mchorror.watcherprotocol.core.memory.PlayerMemorySystem;
import com.mchorror.watcherprotocol.phases.phase1.MobDisruptionSystem;
import com.mchorror.watcherprotocol.registry.WatcherItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Watcher_protocol implements ModInitializer {
    public static final String MOD_ID = "watcher_protocol";

    private static final String GUIDE_TAG = MOD_ID + ".guide_received";
    private static final Identifier GUIDE_JSON_ID = new Identifier(MOD_ID, "kubejs_guide/guide_pages.json");
    private static final String GUIDE_JSON_CLASSPATH = "data/watcher_protocol/kubejs_guide/guide_pages.json";

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
            if (player.getCommandTags().contains(GUIDE_TAG)) return;

            ItemStack guideBook = createGuideFromJson(server);
            if (!player.getInventory().insertStack(guideBook.copy())) {
                player.dropItem(guideBook.copy(), false);
            }
            player.addCommandTag(GUIDE_TAG);
        });
    }
    private static ItemStack createGuideFromJson(MinecraftServer server) {
        ItemStack manual = new ItemStack(Items.WRITTEN_BOOK);
        NbtCompound nbt = manual.getOrCreateNbt();
        nbt.putString("title", "Watcher Survival Manual");
        nbt.putString("author", "KubeJS Archive");
        NbtList pages = new NbtList();

        loadPagesFromServerData(server, pages);
        if (pages.isEmpty()) {
            loadPagesFromClasspath(pages);
        }
        if (pages.isEmpty()) {
            pages.add(NbtString.of(jsonPage("Watcher Survival Manual\\n\\nGuide data missing. Check kubejs_guide JSON resources.")));
        }

        nbt.put("pages", pages);
        return manual;
    }

    private static void loadPagesFromServerData(MinecraftServer server, NbtList pages) {
        try {
            var optResource = server.getResourceManager().getResource(GUIDE_JSON_ID);
            if (optResource.isPresent()) {
                try (InputStream stream = optResource.get().getInputStream()) {
                    appendPagesFromStream(stream, pages);
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Failed loading guide from server data resources", ex);
        }
    }

    private static void loadPagesFromClasspath(NbtList pages) {
        try (InputStream stream = Watcher_protocol.class.getClassLoader().getResourceAsStream(GUIDE_JSON_CLASSPATH)) {
            if (stream != null) {
                appendPagesFromStream(stream, pages);
            }
        } catch (Exception ex) {
            LOGGER.warn("Failed loading guide from classpath resources", ex);
        }
    }

    private static void appendPagesFromStream(InputStream stream, NbtList pages) {
        JsonObject root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonArray jsonPages = root.getAsJsonArray("pages");
        for (var e : jsonPages) {
            pages.add(NbtString.of(jsonPage(e.getAsString())));
        }
    }

    private static String jsonPage(String text) {
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return "{\"text\":\"" + escaped + "\"}";
    }
}