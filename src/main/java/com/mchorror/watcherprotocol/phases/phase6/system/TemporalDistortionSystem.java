package com.mchorror.watcherprotocol.phases.phase6.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class TemporalDistortionSystem implements PhaseSixSystem {
    private static final int MIN_INTERVAL = 50;
    private int cooldown = MIN_INTERVAL;
    private int frozenSkyTicks;
    private long frozenTimeOfDay;
    private final Map<UUID, Vec3d> previousPositions = new HashMap<>();

    @Override
    public void onStart(ServerWorld world, double corruptionLevel) {
        cooldown = MIN_INTERVAL;
        frozenSkyTicks = 0;
        previousPositions.clear();
    }

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        if (frozenSkyTicks > 0) {
            frozenSkyTicks--;
            world.setTimeOfDay(frozenTimeOfDay);
        }

        List<ServerPlayerEntity> players = world.getPlayers();
        for (ServerPlayerEntity player : players) {
            Vec3d last = previousPositions.put(player.getUuid(), player.getPos());
            if (last != null && world.getRandom().nextFloat() < 0.012f * (float) corruptionLevel) {
                Vec3d rewind = last.subtract(player.getPos());
                if (rewind.lengthSquared() < 6.0) {
                    player.requestTeleport(last.x, last.y, last.z);
                    world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_BREATH, SoundCategory.AMBIENT, 0.7f, 0.8f);
                }
            }
        }

        if (--cooldown > 0 || players.isEmpty()) {
            return;
        }

        cooldown = Math.max(15, (int) Math.round((MIN_INTERVAL + world.getRandom().nextBetween(0, 100)) / corruptionLevel));
        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));

        if (world.getRandom().nextFloat() < 0.24f * (float) corruptionLevel) {
            frozenSkyTicks = world.getRandom().nextBetween(200, 800);
            frozenTimeOfDay = world.getTimeOfDay();
        }

        if (world.isDay() && world.getRandom().nextFloat() < 0.25f * (float) corruptionLevel) {
            BlockPos spawnPos = target.getBlockPos().add(world.getRandom().nextBetween(-8, 8), 1, world.getRandom().nextBetween(-8, 8));
            HostileEntity hostile = EntityType.ZOMBIE.create(world);
            if (hostile != null) {
                hostile.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
                world.spawnEntity(hostile);
            }
        }

        if (world.getRandom().nextFloat() < 0.3f * (float) corruptionLevel) {
            PhantomEntity phantom = EntityType.PHANTOM.create(world);
            if (phantom != null) {
                phantom.refreshPositionAndAngles(target.getX(), target.getY() + 18.0, target.getZ(), 0.0f, 0.0f);
                phantom.setTarget(target);
                world.spawnEntity(phantom);
            }
        }

        if (world.getTimeOfDay() % 24000L > 18000L && world.getRandom().nextFloat() < 0.2f * (float) corruptionLevel) {
            target.sendMessage(Text.literal("You can't sleep now."), true);
        }
    }
}