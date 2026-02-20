package com.mchorror.watcherprotocol.phases.phase6.system;

import com.mchorror.watcherprotocol.core.memory.PlayerMemorySystem;
import com.mchorror.watcherprotocol.core.memory.PlayerPatternMemory;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class WorldPersistenceCorruptionSystem implements PhaseSixSystem {
    private int cooldown = 110;

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty() || --cooldown > 0) {
            return;
        }
        cooldown = Math.max(40, (int) Math.round((180 + world.getRandom().nextBetween(0, 200)) / corruptionLevel));

        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));
        PlayerPatternMemory memory = PlayerMemorySystem.getMemory(target.getUuid());
        if (memory == null || memory.getPlacedBlockPattern().size() < 3) {
            return;
        }

        BlockPos base = memory.getPlacedBlockPattern().get(0);
        BlockPos offset = target.getBlockPos().add(world.getRandom().nextBetween(6, 10), 0, world.getRandom().nextBetween(6, 10));
        for (int i = 0; i < Math.min(8, memory.getPlacedBlockPattern().size()); i++) {
            BlockPos relative = memory.getPlacedBlockPattern().get(i).subtract(base);
            BlockPos ghostPos = offset.add(relative);
            if (world.getBlockState(ghostPos).isAir()) {
                world.setBlockState(ghostPos, Blocks.TINTED_GLASS.getDefaultState(), 3);
            }
        }

        world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.AMBIENT, 0.35f, 0.6f);
    }
}
