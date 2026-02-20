package com.mchorror.watcherprotocol.phases.phase6.system;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;

import static net.minecraft.sound.SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP;

public class BiomeCorruptionSystem implements PhaseSixSystem {
    private final Set<Long> corruptedChunks = new HashSet<>();
    private int cooldown = 80;

    @Override
    public void onStart(ServerWorld world, double corruptionLevel) {
        corruptedChunks.clear();
        cooldown = 80;
    }

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        for (ServerPlayerEntity player : players) {
            if (world.getRandom().nextFloat() < 0.30f * (float) corruptionLevel) {
                ChunkPos chunkPos = new ChunkPos(player.getBlockPos());
                corruptedChunks.add(ChunkPos.toLong(chunkPos.x, chunkPos.z));
            }
        }

        if (--cooldown > 0) {
            return;
        }
        cooldown = Math.max(30, (int) Math.round((120 + world.getRandom().nextBetween(0, 140)) / corruptionLevel));

        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));
        ChunkPos chunkPos = new ChunkPos(target.getBlockPos());
        if (!corruptedChunks.contains(ChunkPos.toLong(chunkPos.x, chunkPos.z))) {
            return;
        }

        if (world.getBiome(target.getBlockPos()).isIn(BiomeTags.IS_BADLANDS)) {
            world.setWeather(0, 600, true, false);
        }

        SoundEvent mismatchAmbient = world.getBiome(target.getBlockPos()).isIn(BiomeTags.IS_NETHER)
                ? SoundEvents.AMBIENT_UNDERWATER_LOOP
                : SoundEvents.BLOCK_LAVA_AMBIENT;
        world.playSound(null, target.getBlockPos(), mismatchAmbient, SoundCategory.AMBIENT, 0.45f, 0.9f);

        if (world.getRandom().nextFloat() < 0.15f * (float) corruptionLevel) {
            target.sendMessage(Text.literal("Biome identity drift detected."), true);
        }
    }
}
