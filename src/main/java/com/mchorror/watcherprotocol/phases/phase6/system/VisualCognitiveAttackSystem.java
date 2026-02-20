package com.mchorror.watcherprotocol.phases.phase6.system;

import java.util.List;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class VisualCognitiveAttackSystem implements PhaseSixSystem {
    private int cooldown = 90;

    @Override
    public void tick(ServerWorld world, double corruptionLevel) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty() || --cooldown > 0) {
            return;
        }
        cooldown = Math.max(20, (int) Math.round((140 + world.getRandom().nextBetween(0, 160)) / corruptionLevel));

        ServerPlayerEntity target = players.get(world.getRandom().nextInt(players.size()));
        Vec3d side = target.getRotationVec(1.0f).crossProduct(new Vec3d(0.0, 1.0, 0.0)).normalize().multiply(4.5);
        Vec3d peripheral = target.getPos().add(side.multiply(world.getRandom().nextBoolean() ? 1.0 : -1.0));
        world.spawnParticles(ParticleTypes.LARGE_SMOKE, peripheral.x, peripheral.y + 1.0, peripheral.z, 8, 0.2, 0.5, 0.2, 0.0);
        world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_STARE, SoundCategory.AMBIENT, 0.35f, 0.7f);

        if (world.getRandom().nextFloat() < 0.25f * (float) corruptionLevel) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 80, 0, true, false, false));
        }
    }
}
