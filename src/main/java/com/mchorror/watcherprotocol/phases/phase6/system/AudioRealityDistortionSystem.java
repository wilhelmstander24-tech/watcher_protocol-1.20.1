package com.mchorror.watcherprotocol.phases.phase6.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class AudioRealityDistortionSystem implements PhaseSixSystem {
    private final Map<UUID, Integer> playerEchoCooldown = new HashMap<>();
    private int cooldown = 60;

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        List<ServerPlayerEntity> players = world.getPlayers();
        for (ServerPlayerEntity player : players) {
            int echo = playerEchoCooldown.getOrDefault(player.getUuid(), 0) - 1;
            if (echo <= 0 && world.getRandom().nextFloat() < 0.08f * (float) corruptionLevel) {
                playCrossDimensionalBleed(world, player);
                playerEchoCooldown.put(player.getUuid(), world.getRandom().nextBetween(15, 45));
            } else {
                playerEchoCooldown.put(player.getUuid(), Math.max(0, echo));
            }
        }

        if (players.isEmpty() || --cooldown > 0) {
            return;
        }
        cooldown = Math.max(20, (int) Math.round((100 + world.getRandom().nextBetween(0, 100)) / corruptionLevel));
        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));
        target.playSound(SoundEvents.BLOCK_STONE_STEP, SoundCategory.AMBIENT, 0.5f, 0.8f);
        target.playSound(SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.4f, 1.15f);
    }

    private static void playCrossDimensionalBleed(ServerWorld world, ServerPlayerEntity player) {
        SoundEvent[] pool = {
                SoundEvents.BLOCK_NETHERRACK_BREAK,
                SoundEvents.ENTITY_ENDER_DRAGON_GROWL,
                SoundEvents.BLOCK_END_PORTAL_SPAWN
        };
        SoundEvent event = pool[world.getRandom().nextInt(pool.length)];
        player.playSound(event, SoundCategory.AMBIENT, 0.45f, 0.9f + world.getRandom().nextFloat() * 0.3f);
    }
}
