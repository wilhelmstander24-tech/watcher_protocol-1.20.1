package com.mchorror.watcherprotocol.phases.phase2;

import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import java.util.List;
import net.minecraft.block.Blocks;
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
        if (--cooldownTicks > 0) {
            return;
        }

        cooldownTicks = EVENT_INTERVAL_TICKS + world.getRandom().nextBetween(0, 180);
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));
        int roll = world.getRandom().nextBetween(0, 99);
        if (roll < 35) {
            playDistantSound(world, target, SoundEvents.BLOCK_STONE_STEP, 12.0, 20.0, 0.22f);
        } else if (roll < 70) {
            playDistantSound(world, target, SoundEvents.BLOCK_STONE_BREAK, 8.0, 18.0, 0.26f);
        } else if (roll < 90) {
            SoundEvent doorSound = world.getRandom().nextBoolean()
                    ? SoundEvents.BLOCK_WOODEN_DOOR_OPEN
                    : SoundEvents.BLOCK_WOODEN_DOOR_CLOSE;
            playDistantSound(world, target, doorSound, 6.0, 16.0, 0.35f);
        } else {
            placeFalseTorch(world, target);
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
        BlockPos soundPos = BlockPos.ofFloored(
                basePos.x + Math.cos(angle) * distance,
                player.getY(),
                basePos.z + Math.sin(angle) * distance);

        world.playSound(null, soundPos, sound, SoundCategory.AMBIENT, volume, 0.9f + world.getRandom().nextFloat() * 0.3f);
    }

    private static void placeFalseTorch(ServerWorld world, ServerPlayerEntity player) {
        BlockPos center = player.getBlockPos();
        for (int i = 0; i < 12; i++) {
            BlockPos pos = center.add(
                    world.getRandom().nextBetween(-6, 6),
                    world.getRandom().nextBetween(-2, 2),
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