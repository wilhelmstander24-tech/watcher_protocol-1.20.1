package com.mchorror.watcherprotocol.phases.phase1;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class DesynchronizationPhase implements Phase {
	private static final int ADJUSTMENT_INTERVAL_TICKS = 600;
	private static final int MAX_VARIANCE = 1;

	private final Map<net.minecraft.registry.RegistryKey<World>, Integer> baselineTickSpeeds = new HashMap<>();
	private int adjustmentCooldown = ADJUSTMENT_INTERVAL_TICKS;

	@Override
	public PhaseType getType() {
		return PhaseType.PHASE_1;
	}

	@Override
	public void onStart(ServerWorld world) {
		baselineTickSpeeds.putIfAbsent(world.getRegistryKey(), getRandomTickSpeed(world));
		adjustmentCooldown = ADJUSTMENT_INTERVAL_TICKS;
	}

	@Override
	public void onStop(ServerWorld world) {
		Integer baseline = baselineTickSpeeds.remove(world.getRegistryKey());
		if (baseline != null) {
			setRandomTickSpeed(world, baseline);
		}
	}

	@Override
	public void tick(ServerWorld world) {
		if (--adjustmentCooldown > 0) {
			return;
		}

		double frequency = WatcherConfigManager.getConfig().getInterruptionFrequency();
		double intensity = WatcherConfigManager.getConfig().getInterferenceIntensity();
		double destructiveness = WatcherConfigManager.getConfig().getWorldDestructiveness();
		adjustmentCooldown = Math.max(120, (int) Math.round(ADJUSTMENT_INTERVAL_TICKS / frequency));
		int baseline = baselineTickSpeeds.computeIfAbsent(world.getRegistryKey(), key -> getRandomTickSpeed(world));
		int allowedVariance = Math.max(0, (int) Math.round(MAX_VARIANCE + intensity * destructiveness * 3.0));
		int variance = world.getRandom().nextBetween(-allowedVariance, allowedVariance);
		int adjusted = Math.max(1, baseline + variance);
		setRandomTickSpeed(world, adjusted);
	}

	private static int getRandomTickSpeed(ServerWorld world) {
		return world.getGameRules().get(GameRules.RANDOM_TICK_SPEED).get();
	}

	private static void setRandomTickSpeed(ServerWorld world, int value) {
		world.getGameRules().get(GameRules.RANDOM_TICK_SPEED).set(value, world.getServer());
	}
}
