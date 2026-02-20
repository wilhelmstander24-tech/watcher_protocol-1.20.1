package com.mchorror.watcherprotocol.phases.phase6.system;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TerrainCorruptionSystem implements PhaseSixSystem {
    private static final Block[] ROT_PALETTE = {Blocks.DEEPSLATE, Blocks.CRYING_OBSIDIAN, Blocks.BLACK_CONCRETE};
    private final Map<UUID, Vec3d> snapback = new ConcurrentHashMap<>();
    private int cooldown = 90;

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        List<ServerPlayerEntity> players = world.getPlayers();
        for (ServerPlayerEntity player : players) {
            Vec3d backPos = snapback.remove(player.getUuid());
            if (backPos != null) {
                player.requestTeleport(backPos.x, backPos.y, backPos.z);
            }
        }

        if (players.isEmpty() || --cooldown > 0) {
            return;
        }
        cooldown = Math.max(30, (int) Math.round((120 + world.getRandom().nextBetween(0, 120)) / corruptionLevel));

        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));

        if (world.getRandom().nextFloat() < 0.35f * (float) corruptionLevel) {
            corruptNearbyBlocks(world, target, corruptionLevel);
        }
        if (world.getRandom().nextFloat() < 0.20f * (float) corruptionLevel) {
            hollowTerrainGlitch(world, target);
        }
        if (world.getRandom().nextFloat() < 0.16f * (float) corruptionLevel && world.isNight()) {
            spawnImpossibleGeometry(world, target);
        }
    }

    private static void corruptNearbyBlocks(ServerWorld world, PlayerEntity player, double corruptionLevel) {
        for (int i = 0; i < Math.max(1, (int) Math.round(3 * corruptionLevel)); i++) {
            BlockPos pos = player.getBlockPos().add(world.getRandom().nextBetween(-8, 8), world.getRandom().nextBetween(-2, 3),
                    world.getRandom().nextBetween(-8, 8));
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || !state.isOpaque()) {
                continue;
            }
            Block replacement = ROT_PALETTE[world.getRandom().nextInt(ROT_PALETTE.length)];
            world.setBlockState(pos, replacement.getDefaultState(), 3);
        }
    }

    private void hollowTerrainGlitch(ServerWorld world, ServerPlayerEntity target) {
        BlockPos below = target.getBlockPos().down();
        if (!world.getBlockState(below).isSolidBlock(world, below)) {
            return;
        }
        snapback.put(target.getUuid(), target.getPos());
        target.requestTeleport(target.getX(), target.getY() - 1.2, target.getZ());
        world.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.BLOCK_STONE_BREAK, SoundCategory.AMBIENT, 0.9f, 0.7f);
    }

    private static void spawnImpossibleGeometry(ServerWorld world, ServerPlayerEntity target) {
        BlockPos base = target.getBlockPos().add(world.getRandom().nextBetween(-12, 12), 3, world.getRandom().nextBetween(-12, 12));
        for (int i = 0; i < 4; i++) {
            BlockPos pos = base.add(world.getRandom().nextBetween(-2, 2), world.getRandom().nextBetween(0, 2), world.getRandom().nextBetween(-2, 2));
            if (world.getBlockState(pos).isAir()) {
                world.setBlockState(pos, Blocks.STONE.getDefaultState(), 3);
            }
        }
    }
}