package com.mchorror.watcherprotocol.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mchorror.watcherprotocol.Watcher_protocol;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class WatcherConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "watcher_protocol.json";

    private static WatcherProtocolConfig config = new WatcherProtocolConfig();

    private WatcherConfigManager() {
    }

    public static void init() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            WatcherProtocolConfig loaded = GSON.fromJson(reader, WatcherProtocolConfig.class);
            if (loaded != null) {
                sanitize(loaded);
                config = loaded;
            }
        } catch (IOException exception) {
            Watcher_protocol.LOGGER.error("Failed to load watcher protocol config.", exception);
        }
    }

    private static void sanitize(WatcherProtocolConfig target) {
        target.setInterferenceIntensity(target.getInterferenceIntensity());
        target.setInterruptionFrequency(target.getInterruptionFrequency());
        target.setWorldDestructiveness(target.getWorldDestructiveness());
    }

    public static WatcherProtocolConfig getConfig() {
        return config;
    }

    public static void save() {
        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException exception) {
            Watcher_protocol.LOGGER.error("Failed to create config directory.", exception);
            return;
        }

        sanitize(config);
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(config, writer);
        } catch (IOException exception) {
            Watcher_protocol.LOGGER.error("Failed to save watcher protocol config.", exception);
        }
    }

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }
}