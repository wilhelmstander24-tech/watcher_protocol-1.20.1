package com.mchorror.watcherprotocol.phases.phase6.system;

import com.mchorror.watcherprotocol.registry.WatcherItems;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class GuideScriptInfluenceSystem implements PhaseSixSystem {
    private int cooldown = 100;

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty() || --cooldown > 0) {
            return;
        }
        cooldown = Math.max(20, (int) Math.round((120 + world.getRandom().nextBetween(0, 140)) / corruptionLevel));

        String[] fragments = {
                "The pages changed while you blinked.",
                "Do not trust the second sunrise.",
                "The Watcher edits your memories.",
                "If time stutters, stand still and breathe."
        };
        String script = fragments[world.getRandom().nextInt(fragments.length)];

        for (ServerPlayerEntity player : players) {
            for (ItemStack stack : player.getInventory().main) {
                if (stack.isOf(WatcherItems.WATCHER_GUIDE)) {
                    stack.getOrCreateNbt().putString("phase6_script", script);
                }
                if (stack.isOf(WatcherItems.ANOMALY_JOURNAL) && world.getRandom().nextFloat() < 0.30f) {
                    int bonusLogs = stack.getOrCreateNbt().getInt("anomalies_logged") + 1;
                    stack.getOrCreateNbt().putInt("anomalies_logged", bonusLogs);
                }
            }
        }
    }
}