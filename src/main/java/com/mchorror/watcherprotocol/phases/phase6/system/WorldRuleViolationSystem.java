package com.mchorror.watcherprotocol.phases.phase6.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class WorldRuleViolationSystem implements PhaseSixSystem {
    private int cooldown = 50;
    private final Map<UUID, Integer> unstableSlots = new HashMap<>();

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        List<ServerPlayerEntity> players = world.getPlayers();
        for (ServerPlayerEntity player : players) {
            Box area = player.getBoundingBox().expand(24.0);
            for (ItemEntity item : world.getEntitiesByClass(ItemEntity.class, area, e -> true)) {
                if (world.getRandom().nextFloat() < 0.02f * (float) corruptionLevel) {
                    item.setVelocity(item.getVelocity().add(world.getRandom().nextDouble() * 0.2 - 0.1, 0.02, world.getRandom().nextDouble() * 0.2 - 0.1));
                }
            }

            for (PersistentProjectileEntity arrow : world.getEntitiesByClass(PersistentProjectileEntity.class, area, e -> true)) {
                if (world.getRandom().nextFloat() < 0.04f * (float) corruptionLevel) {
                    Vec3d side = new Vec3d(world.getRandom().nextDouble() - 0.5, 0.0, world.getRandom().nextDouble() - 0.5).normalize().multiply(0.12);
                    arrow.setVelocity(arrow.getVelocity().add(side));
                }
            }
        }

        if (players.isEmpty() || --cooldown > 0) {
            return;
        }
        cooldown = Math.max(20, (int) Math.round((100 + world.getRandom().nextBetween(0, 120)) / corruptionLevel));

        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));
        int unstable = world.getRandom().nextBetween(0, 8);
        unstableSlots.put(target.getUuid(), unstable);
        target.sendMessage(Text.literal("Slot " + (unstable + 1) + " feels unstable."), true);
        if (target.getInventory().selectedSlot == unstable && world.getRandom().nextBoolean()) {
            target.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.PLAYERS, 0.7f, 0.5f);
        }

        for (int i = 0; i < 8; i++) {
            BlockPos pos = target.getBlockPos().add(world.getRandom().nextBetween(-8, 8), world.getRandom().nextBetween(-2, 2),
                    world.getRandom().nextBetween(-8, 8));
            BlockState state = world.getBlockState(pos);
            if (state.contains(Properties.HORIZONTAL_FACING)) {
                var facing = state.get(Properties.HORIZONTAL_FACING);
                world.setBlockState(pos, state.with(Properties.HORIZONTAL_FACING, facing.rotateYClockwise()), 3);
                return;
            }
            if (state.isOf(Blocks.DIRT_PATH)) {
                world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 3);
                return;
            }
        }
    }
}