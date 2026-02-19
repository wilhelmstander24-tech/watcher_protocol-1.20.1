package com.mchorror.watcherprotocol.phases.phase2;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PerceptualCorruptionPhase implements Phase {
	private static final int EVENT_INTERVAL_TICKS = 220;
	private int cooldownTicks = EVENT_INTERVAL_TICKS;

	@Override
	public PhaseType getType() {
		return PhaseType.PHASE_2;
	}

	@Override
	public void onStart(ServerWorld world) {
		cooldownTicks = EVENT_INTERVAL_TICKS;
	}

	@Override
	public void tick(ServerWorld world) {
		double frequency = WatcherConfigManager.getConfig().getInterruptionFrequency();
		if (--cooldownTicks > 0) {
			return;
		}

		cooldownTicks = Math.max(60, (int) Math.round((EVENT_INTERVAL_TICKS + world.getRandom().nextBetween(0, 180)) / frequency));
		List<ServerPlayerEntity> players = world.getPlayers();
		if (players.isEmpty()) {
			return;
		}

		ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));
		double intensity = WatcherConfigManager.getConfig().getInterferenceIntensity();
		int roll = world.getRandom().nextBetween(0, 99);
		if (roll < 30) {
			playBiomeAwareSound(world, target, 12.0, 20.0, (float) (0.22f * intensity));
		} else if (roll < 65) {
			playDistantSound(world, target, SoundEvents.BLOCK_STONE_BREAK, 8.0, 18.0, (float) (0.2f * intensity));
		} else if (roll < 90) {
			playDoorNoiseIfNearby(world, target, (float) (0.35f * intensity));
		} else if (WatcherConfigManager.getConfig().getWorldDestructiveness() > 0.2) {
			placeFalseTorch(world, target);
		}
	}

	private static void playBiomeAwareSound(ServerWorld world, ServerPlayerEntity player, double minDistance, double maxDistance,
			float volume) {
		SoundEvent sound;
		BlockPos playerPos = player.getBlockPos();
		if (world.getBiome(playerPos).isIn(BiomeTags.IS_NETHER)) {
			sound = SoundEvents.BLOCK_NETHERRACK_BREAK;
		} else if (world.getBiome(playerPos).isIn(BiomeTags.IS_OCEAN)) {
			sound = SoundEvents.AMBIENT_UNDERWATER_LOOP;
		} else if (world.getBiome(playerPos).isIn(BiomeTags.IS_SNOWY)) {
			sound = SoundEvents.BLOCK_SNOW_STEP;
		} else {
			sound = SoundEvents.BLOCK_STONE_STEP;
		}
		playDistantSound(world, player, sound, minDistance, maxDistance, volume);
	}

	private static void playDoorNoiseIfNearby(ServerWorld world, ServerPlayerEntity player, float volume) {
		BlockPos playerPos = player.getBlockPos();
		for (int i = 0; i < 20; i++) {
			BlockPos pos = playerPos.add(world.getRandom().nextBetween(-8, 8), world.getRandom().nextBetween(-2, 2),
					world.getRandom().nextBetween(-8, 8));
			BlockState state = world.getBlockState(pos);
			if (!(state.getBlock() instanceof DoorBlock)) {
				continue;
			}
			SoundEvent doorSound = world.getRandom().nextBoolean() ? SoundEvents.BLOCK_WOODEN_DOOR_OPEN
					: SoundEvents.BLOCK_WOODEN_DOOR_CLOSE;
			world.playSound(null, pos, doorSound, SoundCategory.AMBIENT, volume, 0.95f + world.getRandom().nextFloat() * 0.2f);
			return;
		}
	}

	private static void playDistantSound(ServerWorld world,
			ServerPlayerEntity player,
			SoundEvent sound,
			double minDistance,
			double maxDistance,
			float volume) {
		Vec3d basePos = player.getPos();
		double distance = minDistance + world.getRandom().nextDouble() * (maxDistance - minDistance);
		double angle = world.getRandom().nextDouble() * Math.PI * 2.0;
		BlockPos soundPos = BlockPos.ofFloored(basePos.x + Math.cos(angle) * distance, player.getY(),
				basePos.z + Math.sin(angle) * distance);

		world.playSound(null, soundPos, sound, SoundCategory.AMBIENT, Math.max(0.05f, volume),
				0.9f + world.getRandom().nextFloat() * 0.3f);
	}

	private static void placeFalseTorch(ServerWorld world, ServerPlayerEntity player) {
		BlockPos center = player.getBlockPos();
		for (int i = 0; i < 12; i++) {
			BlockPos pos = center.add(world.getRandom().nextBetween(-6, 6), world.getRandom().nextBetween(-2, 2),
					world.getRandom().nextBetween(-6, 6));

			if (!world.getBlockState(pos).isAir()) {
				continue;
			}
			if (!world.getBlockState(pos.down()).isSolidBlock(world, pos.down())) {
				continue;
			}
			if (!Blocks.TORCH.getDefaultState().canPlaceAt(world, pos)) {
				continue;
			}

			world.setBlockState(pos, Blocks.TORCH.getDefaultState(), 3);
			return;
		}
	}
}
