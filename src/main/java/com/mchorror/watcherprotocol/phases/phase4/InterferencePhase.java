package com.mchorror.watcherprotocol.phases.phase4;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class InterferencePhase implements Phase {
    private static final int EVENT_INTERVAL_TICKS = 140;
    private int cooldownTicks = EVENT_INTERVAL_TICKS;

    @Override
    public PhaseType getType() {
        return PhaseType.PHASE_4;
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

        cooldownTicks = Math.max(40,
                (int) Math.round((EVENT_INTERVAL_TICKS + world.getRandom().nextBetween(0, 120)) / frequency));
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));
        double intensity = WatcherConfigManager.getConfig().getInterferenceIntensity();
        double destructiveness = WatcherConfigManager.getConfig().getWorldDestructiveness();
        disturbDoorsAtNight(world, target, intensity);
        uprootNearbyCrops(world, target, destructiveness);
        if (destructiveness > 0.45 && target.getY() < world.getSeaLevel() - 4) {
            carveTunnelBehindPlayer(world, target);
        }
        bleedBiomeSurface(world, target, intensity, destructiveness);
        randomizeMobHostility(world, target, intensity);
    }

    private static void disturbDoorsAtNight(ServerWorld world, ServerPlayerEntity player, double intensity) {
        if (!world.isNight()) {
            return;
        }

        for (int i = 0; i < 14; i++) {
            BlockPos pos = player.getBlockPos().add(world.getRandom().nextBetween(-8, 8), world.getRandom().nextBetween(-2, 2),
                    world.getRandom().nextBetween(-8, 8));
            BlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof DoorBlock) || !state.contains(Properties.OPEN)) {
                continue;
            }

            if (!state.get(Properties.OPEN) && world.getRandom().nextFloat() < 0.45f * (float) intensity) {
                world.setBlockState(pos, state.with(Properties.OPEN, true), 3);
            }
            return;
        }
    }

    private static void uprootNearbyCrops(ServerWorld world, ServerPlayerEntity player, double destructiveness) {
        if (destructiveness < 0.25) {
            return;
        }
        for (int i = 0; i < 12; i++) {
            BlockPos pos = player.getBlockPos().add(world.getRandom().nextBetween(-10, 10), world.getRandom().nextBetween(-2, 2),
                    world.getRandom().nextBetween(-10, 10));
            BlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof CropBlock) || !world.getBlockState(pos.down()).isOf(Blocks.FARMLAND)) {
                continue;
            }
            world.breakBlock(pos, false);
            return;
        }
    }

    private static void carveTunnelBehindPlayer(ServerWorld world, ServerPlayerEntity player) {
        Direction facing = player.getHorizontalFacing().getOpposite();
        BlockPos start = player.getBlockPos().offset(facing, 2);
        for (int i = 0; i < 4; i++) {
            BlockPos base = start.offset(facing, i);
            for (int y = 0; y <= 1; y++) {
                BlockPos carvePos = base.up(y);
                BlockState state = world.getBlockState(carvePos);
                if (state.isOf(Blocks.STONE) || state.isOf(Blocks.DIRT) || state.isOf(Blocks.DEEPSLATE) || state.isOf(Blocks.GRAVEL)
                        || state.isOf(Blocks.COBBLESTONE)) {
                    world.setBlockState(carvePos, Blocks.AIR.getDefaultState(), 3);
                }
            }
        }
    }

    private static void bleedBiomeSurface(ServerWorld world, ServerPlayerEntity player, double intensity, double destructiveness) {
        if (destructiveness < 0.2) {
            return;
        }
        BlockPos center = player.getBlockPos();
        for (int i = 0; i < 10; i++) {
            BlockPos top = world.getTopPosition(net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                    center.add(world.getRandom().nextBetween(-12, 12), 0, world.getRandom().nextBetween(-12, 12))).down();
            BlockState state = world.getBlockState(top);
            if (!state.isOf(Blocks.GRASS_BLOCK) && !state.isOf(Blocks.DIRT)) {
                continue;
            }

            Block replacement = pickBiomeBleedBlock(world, top);
            if (world.getRandom().nextFloat() < 0.7f * (float) intensity) {
                world.setBlockState(top, replacement.getDefaultState(), 3);
            }
            return;
        }
    }

    private static Block pickBiomeBleedBlock(ServerWorld world, BlockPos pos) {
        if (world.getBiome(pos).isIn(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE)) {
            return world.getRandom().nextBoolean() ? Blocks.SAND : Blocks.RED_SAND;
        }
        if (world.getBiome(pos).isIn(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
            return Blocks.SNOW_BLOCK;
        }
        if (world.getBiome(pos).isIn(BiomeTags.IS_NETHER)) {
            return Blocks.SOUL_SOIL;
        }
        if (world.getBiome(pos).isIn(BiomeTags.IS_JUNGLE)) {
            return Blocks.MOSS_BLOCK;
        }
        return Blocks.MYCELIUM;
    }

    private static void randomizeMobHostility(ServerWorld world, ServerPlayerEntity player, double intensity) {
        Box area = player.getBoundingBox().expand(20.0);
        for (MobEntity mob : world.getEntitiesByClass(MobEntity.class, area, m -> true)) {
            if (mob instanceof HostileEntity hostile) {
                if (world.getRandom().nextFloat() < 0.30f * (float) intensity) {
                    hostile.setTarget(player);
                }
                if (world.getRandom().nextFloat() < 0.12f * (float) intensity) {
                    hostile.getNavigation().stop();
                }
                if (world.getRandom().nextFloat() < 0.14f * (float) intensity) {
                    Vec3d jitter = new Vec3d(world.getRandom().nextDouble() - 0.5, 0.0, world.getRandom().nextDouble() - 0.5)
                            .normalize().multiply(6.0);
                    hostile.getNavigation().startMovingTo(mob.getX() + jitter.x, mob.getY(), mob.getZ() + jitter.z, 1.25);
                }
            }

            if (mob instanceof Angerable angerable && world.getRandom().nextFloat() < 0.22f * (float) intensity) {
                angerable.setAngryAt(player.getUuid());
                mob.setTarget(player);
            }

            if (mob instanceof PassiveEntity passive && world.getRandom().nextFloat() < 0.05f * (float) intensity) {
                passive.setTarget(player);
            }
        }
    }
}