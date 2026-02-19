package com.mchorror.watcherprotocol.phases.phase5;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.core.memory.PlayerMemorySystem;
import com.mchorror.watcherprotocol.core.memory.PlayerPatternMemory;
import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MockeryPhase implements Phase {
    private static final int MIN_EVENT_GAP = 80;
    private static final int RANDOM_GAP = 80;
    private int cooldownTicks = MIN_EVENT_GAP;

    @Override
    public PhaseType getType() {
        return PhaseType.PHASE_5;
    }

    @Override
    public void onStart(ServerWorld world) {
        cooldownTicks = MIN_EVENT_GAP;
    }

    @Override
    public void tick(ServerWorld world) {
        double frequency = WatcherConfigManager.getConfig().getInterruptionFrequency();
        if (--cooldownTicks > 0) {
            return;
        }

        cooldownTicks = Math.max(30, (int) Math.round((MIN_EVENT_GAP + world.getRandom().nextBetween(0, RANDOM_GAP)) / frequency));
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }

        ServerPlayerEntity player = players.get(world.getRandom().nextInt(players.size()));
        PlayerPatternMemory memory = PlayerMemorySystem.getMemory(player.getUuid());
        if (memory == null) {
            return;
        }

        echoFootsteps(world, player, memory);
        mimicMiningRhythm(world, player, memory);
        if (WatcherConfigManager.getConfig().getWorldDestructiveness() > 0.35) {
            buildDistortedDuplicate(world, player, memory);
        }
        manifestMovementPattern(world, player, memory);
    }

    private static void echoFootsteps(ServerWorld world, ServerPlayerEntity player, PlayerPatternMemory memory) {
        if (memory.getMovementIntervals().isEmpty()) {
            return;
        }

        int lastInterval = memory.getMovementIntervals().get(memory.getMovementIntervals().size() - 1);
        if (lastInterval > 8) {
            return;
        }

        Vec3d behind = player.getPos().subtract(player.getRotationVec(1.0f).multiply(2.0));
        BlockSoundGroup stepSoundGroup = world.getBlockState(player.getSteppingPos()).getSoundGroup();
        world.playSound(null, behind.x, behind.y, behind.z, stepSoundGroup.getStepSound(), SoundCategory.AMBIENT,
                (float) (0.7f + 0.2f * WatcherConfigManager.getConfig().getInterferenceIntensity()),
                0.85f + world.getRandom().nextFloat() * 0.2f);
    }

    private static void mimicMiningRhythm(ServerWorld world, ServerPlayerEntity player, PlayerPatternMemory memory) {
        if (memory.getMiningIntervals().isEmpty()) {
            return;
        }

        int interval = memory.getMiningIntervals().get(memory.getMiningIntervals().size() - 1);
        if (interval > 30) {
            return;
        }

        BlockPos pos = player.getBlockPos().add(world.getRandom().nextBetween(-4, 4), world.getRandom().nextBetween(-2, 1),
                world.getRandom().nextBetween(-4, 4));
        world.playSound(null, pos, pickBiomeMiningSound(world, player.getBlockPos()), SoundCategory.BLOCKS, 0.8f, 0.8f);
    }

    private static SoundEvent pickBiomeMiningSound(ServerWorld world, BlockPos pos) {
        if (world.getBiome(pos).isIn(BiomeTags.IS_NETHER)) {
            return SoundEvents.BLOCK_NETHERRACK_BREAK;
        }
        if (world.getBiome(pos).isIn(BiomeTags.VILLAGE_SNOWY_HAS_STRUCTURE)) {
            return SoundEvents.BLOCK_SNOW_BREAK;
        }
        if (world.getBiome(pos).isIn(BiomeTags.IS_OCEAN)) {
            return SoundEvents.BLOCK_GRAVEL_BREAK;
        }
        return SoundEvents.BLOCK_STONE_BREAK;
    }

    private static void buildDistortedDuplicate(ServerWorld world, ServerPlayerEntity player, PlayerPatternMemory memory) {
        if (memory.getPlacedBlockPattern().size() < 4) {
            return;
        }

        BlockPos anchor = player.getBlockPos().add(world.getRandom().nextBetween(-14, 14), -2, world.getRandom().nextBetween(-14, 14));
        List<BlockPos> pattern = memory.getPlacedBlockPattern();
        BlockPos base = pattern.get(0);

        for (int i = 0; i < pattern.size() && i < 12; i++) {
            BlockPos relative = pattern.get(i).subtract(base);
            BlockPos target = anchor.add(relative);
            BlockState state = distortedBlock(world);
            if (world.getBlockState(target).isAir()) {
                world.setBlockState(target, state, 3);
            }
        }
    }

    private static BlockState distortedBlock(ServerWorld world) {
        Block[] palette = {Blocks.COBBLESTONE, Blocks.DEEPSLATE, Blocks.TUFF, Blocks.GRAVEL, Blocks.AMETHYST_BLOCK};
        Block selected = palette[world.getRandom().nextInt(palette.length)];
        return selected.getDefaultState();
    }

    private static void manifestMovementPattern(ServerWorld world, ServerPlayerEntity player, PlayerPatternMemory memory) {
        List<Vec3d> path = memory.getRecentPath();
        if (path.size() < 3) {
            return;
        }

        int points = Math.min(8, path.size());
        for (int i = 0; i < points; i++) {
            Vec3d source = path.get(path.size() - 1 - i);
            Vec3d shifted = source.add(world.getRandom().nextDouble() * 1.4 - 0.7, 0.1,
                    world.getRandom().nextDouble() * 1.4 - 0.7);
            world.spawnParticles(ParticleTypes.SOUL, shifted.x, shifted.y, shifted.z, 1, 0.01, 0.05, 0.01, 0.0);
        }
    }
}
