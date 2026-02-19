package com.mchorror.watcherprotocol.core;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

public final class MentalMeltdownSystem {
	private static final float MAX_MELTDOWN = 100.0f;

	private static final Map<UUID, Float> meltdownByPlayer = new HashMap<>();
	private static final Map<UUID, net.minecraft.util.math.Vec3d> lastPositionByPlayer = new HashMap<>();

	private MentalMeltdownSystem() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MentalMeltdownSystem::onServerTick);
	}

	public static float getMeltdown(ServerPlayerEntity player) {
		return meltdownByPlayer.getOrDefault(player.getUuid(), MAX_MELTDOWN);
	}

	private static void onServerTick(MinecraftServer server) {
		if (!WatcherConfigManager.getConfig().isModEnabled()) {
			return;
		}

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			updatePlayerMeltdown(player);
		}
	}

	private static void updatePlayerMeltdown(ServerPlayerEntity player) {
		UUID id = player.getUuid();
		float meltdown = meltdownByPlayer.getOrDefault(id, MAX_MELTDOWN);
		int timeSinceRest = player.getStatHandler().getStat(Stats.CUSTOM, Stats.TIME_SINCE_REST);

		net.minecraft.util.math.Vec3d currentPos = player.getPos();
		net.minecraft.util.math.Vec3d lastPos = lastPositionByPlayer.put(id, currentPos);
		boolean moving = lastPos != null && currentPos.squaredDistanceTo(lastPos) > 0.0008;

		float drain = 0.0f;
		if (timeSinceRest > 24000) {
			drain += Math.min(0.08f, (timeSinceRest - 24000) / 24000.0f * 0.02f + 0.01f);
		}
		if (moving && !player.isSleeping()) {
			drain += 0.03f;
		}

		float recovery = 0.0f;
		if (player.isSleeping()) {
			recovery += 0.30f;
		} else if (!moving) {
			recovery += 0.012f;
		}

		meltdown = Math.max(0.0f, Math.min(MAX_MELTDOWN, meltdown - drain + recovery));
		meltdownByPlayer.put(id, meltdown);
	}
}
