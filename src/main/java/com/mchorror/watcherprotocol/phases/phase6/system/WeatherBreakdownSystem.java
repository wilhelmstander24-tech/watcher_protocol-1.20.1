package com.mchorror.watcherprotocol.phases.phase6.system;

import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class WeatherBreakdownSystem implements PhaseSixSystem {
    private int cooldown = 70;

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            return;
        }
        if (--cooldown > 0) {
            return;
        }

        cooldown = Math.max(20, (int) Math.round((90 + world.getRandom().nextBetween(0, 80)) / corruptionLevel));
        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));

        world.setWeather(0, 500, true, world.getRandom().nextFloat() < 0.35f * (float) corruptionLevel);
        for (int i = 0; i < 18; i++) {
            world.spawnParticles(ParticleTypes.RAIN,
                    target.getX() + world.getRandom().nextBetween(-8, 8),
                    target.getY() + world.getRandom().nextDouble() * 5.0,
                    target.getZ() + world.getRandom().nextBetween(-8, 8),
                    1,
                    0.0,
                    0.25,
                    0.0,
                    -0.2);
        }

        if (world.getRandom().nextFloat() < 0.22f * (float) corruptionLevel) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                BlockPos pos = target.getBlockPos().add(world.getRandom().nextBetween(-6, 6), 0, world.getRandom().nextBetween(-6, 6));
                lightning.refreshPositionAfterTeleport(pos.getX(), pos.getY(), pos.getZ());
                lightning.setCosmetic(true);
                world.spawnEntity(lightning);
            }
        }

        target.playSound(SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.AMBIENT, 0.8f, 0.8f);
    }
}