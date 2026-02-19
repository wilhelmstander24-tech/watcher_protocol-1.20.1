package com.mchorror.watcherprotocol.phases.phase3;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.util.math.Vec3d;

public final class PhaseThreeClientEffects {
	private static final long PHASE_THREE_START_TICKS = 24000L * 20L;

	private PhaseThreeClientEffects() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(PhaseThreeClientEffects::onClientTick);
	}

	private static void onClientTick(MinecraftClient client) {
		if (!isPhaseThreeActive(client)) {
			return;
		}

		ClientPlayerEntity player = client.player;
		ClientWorld world = client.world;
		if (player == null || world == null) {
			return;
		}

		spawnShadowFigureInFog(world, player);
		spawnImpossibleWaterReflection(world, player);
		applyCameraMicroTilt(world, player);
		playBreathingAtFullHealth(world, player);
	}

	private static void spawnShadowFigureInFog(ClientWorld world, ClientPlayerEntity player) {
		if (world.getRandom().nextFloat() > 0.04f) {
			return;
		}
		int skyLight = world.getLightLevel(LightType.SKY, player.getBlockPos());
		if (!world.isRaining() && skyLight > 10) {
			return;
		}

		double angle = world.getRandom().nextDouble() * Math.PI * 2.0;
		double distance = 12.0 + world.getRandom().nextDouble() * 12.0;
		double x = player.getX() + Math.cos(angle) * distance;
		double z = player.getZ() + Math.sin(angle) * distance;
		double y = player.getY();

		for (int i = 0; i < 8; i++) {
			world.addParticle(ParticleTypes.SMOKE, x, y + i * 0.24, z, 0.0, 0.0, 0.0);
		}
	}

	private static void spawnImpossibleWaterReflection(ClientWorld world, ClientPlayerEntity player) {
		if (world.getRandom().nextFloat() > 0.03f) {
			return;
		}

		BlockPos playerPos = player.getBlockPos();
		for (int i = 0; i < 10; i++) {
			BlockPos samplePos = playerPos.add(
					world.getRandom().nextBetween(-8, 8),
					world.getRandom().nextBetween(-3, 1),
					world.getRandom().nextBetween(-8, 8));
			BlockState sample = world.getBlockState(samplePos);
			if (!sample.isOf(Blocks.WATER)) {
				continue;
			}

			Vec3d eye = player.getEyePos();
			double reflectedX = samplePos.getX() + 0.5 + (samplePos.getX() + 0.5 - eye.x) * 0.35;
			double reflectedZ = samplePos.getZ() + 0.5 + (samplePos.getZ() + 0.5 - eye.z) * 0.35;
			world.addParticle(ParticleTypes.END_ROD, reflectedX, samplePos.getY() + 1.01, reflectedZ, 0.0, 0.002, 0.0);
			return;
		}
	}

	private static void applyCameraMicroTilt(ClientWorld world, ClientPlayerEntity player) {
		if (world.getRandom().nextFloat() > 0.08f) {
			return;
		}

		float yawOffset = (world.getRandom().nextFloat() - 0.5f) * 0.45f;
		float pitchOffset = (world.getRandom().nextFloat() - 0.5f) * 0.22f;
		player.setYaw(player.getYaw() + yawOffset);
		player.setPitch(player.getPitch() + pitchOffset);
	}

	private static void playBreathingAtFullHealth(ClientWorld world, ClientPlayerEntity player) {
		if (player.getHealth() < player.getMaxHealth()) {
			return;
		}
		if (world.getRandom().nextFloat() > 0.01f) {
			return;
		}

		world.playSound(player.getX(), player.getY(), player.getZ(),
				SoundEvents.ENTITY_HORSE_BREATHE,
				SoundCategory.AMBIENT,
				0.12f,
				0.6f + world.getRandom().nextFloat() * 0.2f,
				false);
	}

	private static boolean isPhaseThreeActive(MinecraftClient client) {
		if (client.world == null) {
			return false;
		}
		return WatcherConfigManager.getConfig().isModEnabled()
				&& client.world.getTimeOfDay() >= PHASE_THREE_START_TICKS;
	}
}
