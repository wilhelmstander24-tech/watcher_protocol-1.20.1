package com.mchorror.watcherprotocol.phases.phase6;

import com.mchorror.watcherprotocol.phases.Phase;
import com.mchorror.watcherprotocol.phases.PhaseType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

public class ConfrontationPhase implements Phase {
    private static final int REQUIRED_STARE_TICKS = 60;
    private static final int RESPAWN_DELAY_TICKS = 80;
    private static final double APPROACH_SPEED = 0.75;
    private static final double STARE_ALIGNMENT = 0.965;
    private static final double MAX_STARE_DISTANCE = 28.0;
    private static final double WATCHER_MAX_HEALTH = 120.0;
    private static final double WATCHER_ATTACK_DAMAGE = 16.0;
    private static final double WATCHER_FOLLOW_RANGE = 56.0;
    private static final double WATCHER_MOVEMENT_SPEED = 0.23;

    private final Map<net.minecraft.registry.RegistryKey<World>, UUID> watcherIds = new HashMap<>();
    private final Map<UUID, Integer> stareTicks = new HashMap<>();
    private int respawnCooldown;

    @Override
    public PhaseType getType() {
        return PhaseType.PHASE_6;
    }

    @Override
    public void onStart(ServerWorld world) {
        respawnCooldown = 0;
        stareTicks.clear();
    }

    @Override
    public void onStop(ServerWorld world) {
        despawnWatcher(world);
    }

    @Override
    public void tick(ServerWorld world) {
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) {
            despawnWatcher(world);
            return;
        }

        if (respawnCooldown > 0) {
            respawnCooldown--;
        }

        EndermanEntity watcher = getOrSpawnWatcher(world, players);
        if (watcher == null) {
            return;
        }

        applyWatcherQualities(watcher);

        boolean staredAt = false;
        for (ServerPlayerEntity player : players) {
            if (isStaringAtWatcher(player, watcher)) {
                staredAt = true;
                int ticks = stareTicks.getOrDefault(player.getUuid(), 0) + 1;
                stareTicks.put(player.getUuid(), ticks);
                if (ticks >= REQUIRED_STARE_TICKS) {
                    despawnWatcher(world);
                    respawnCooldown = RESPAWN_DELAY_TICKS;
                    stareTicks.clear();
                    return;
                }
            } else {
                stareTicks.remove(player.getUuid());
            }
        }

        ServerPlayerEntity nearest = (ServerPlayerEntity) world.getClosestPlayer(watcher, WATCHER_FOLLOW_RANGE);
        if (nearest == null) {
            return;
        }

        watcher.setAngryAt(nearest.getUuid());
        watcher.setTarget(nearest);
        if (!staredAt) {
            randomizeHostileBehavior(world, watcher, nearest);
        }
    }

    private EndermanEntity getOrSpawnWatcher(ServerWorld world, List<ServerPlayerEntity> players) {
        UUID watcherId = watcherIds.get(world.getRegistryKey());
        if (watcherId != null) {
            Entity existing = world.getEntity(watcherId);
            if (existing instanceof EndermanEntity enderman && enderman.isAlive()) {
                return enderman;
            }
            watcherIds.remove(world.getRegistryKey());
        }

        if (respawnCooldown > 0) {
            return null;
        }

        ServerPlayerEntity anchor = players.get(world.getRandom().nextInt(players.size()));
        EndermanEntity watcher = EntityType.ENDERMAN.create(world);
        if (watcher == null) {
            return null;
        }

        BlockPos spawnPos = pickSpawnPos(world, anchor);
        watcher.refreshPositionAndAngles(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                world.getRandom().nextFloat() * 360.0f, 0.0f);
        watcher.setCustomName(Text.literal("The Watcher"));
        applyWatcherQualities(watcher);
        world.spawnEntity(watcher);
        watcherIds.put(world.getRegistryKey(), watcher.getUuid());
        return watcher;
    }

    private static void applyWatcherQualities(EndermanEntity watcher) {
        watcher.setSilent(true);
        watcher.setCustomNameVisible(false);

        EntityAttributeInstance maxHealth = watcher.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(WATCHER_MAX_HEALTH);
        }
        EntityAttributeInstance attackDamage = watcher.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(WATCHER_ATTACK_DAMAGE);
        }
        EntityAttributeInstance movementSpeed = watcher.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.setBaseValue(WATCHER_MOVEMENT_SPEED);
        }
        EntityAttributeInstance followRange = watcher.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
        if (followRange != null) {
            followRange.setBaseValue(WATCHER_FOLLOW_RANGE);
        }
        if (watcher.getHealth() < WATCHER_MAX_HEALTH) {
            watcher.setHealth((float) WATCHER_MAX_HEALTH);
        }

        watcher.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 120, 1, true, false, false));
        watcher.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 120, 0, true, false, false));
        watcher.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 120, 0, true, false, false));
    }

    private static void randomizeHostileBehavior(ServerWorld world, EndermanEntity watcher, ServerPlayerEntity target) {
        double roll = world.getRandom().nextDouble();
        if (roll < 0.20) {
            Vec3d aroundTarget = target.getPos().add(
                    world.getRandom().nextBetween(-6, 6),
                    0.0,
                    world.getRandom().nextBetween(-6, 6));
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) aroundTarget.x, (int) aroundTarget.z);
            watcher.requestTeleport(aroundTarget.x, y, aroundTarget.z);
            world.spawnParticles(ParticleTypes.PORTAL, watcher.getX(), watcher.getY() + 1.0, watcher.getZ(), 16, 0.4, 0.7, 0.4, 0.01);
        } else if (roll < 0.45) {
            Vec3d sideVector = target.getRotationVec(1.0f).crossProduct(new Vec3d(0.0, 1.0, 0.0)).normalize().multiply(4.0);
            Vec3d flankPos = target.getPos().add(sideVector.multiply(world.getRandom().nextBoolean() ? 1.0 : -1.0));
            watcher.getNavigation().startMovingTo(flankPos.x, flankPos.y, flankPos.z, APPROACH_SPEED * 0.9);
        } else if (roll < 0.70) {
            watcher.getNavigation().stop();
            world.spawnParticles(ParticleTypes.PORTAL, watcher.getX(), watcher.getY() + 1.0, watcher.getZ(), 16, 0.4, 0.7, 0.4, 0.01);
        } else {
            watcher.getNavigation().startMovingTo(target, APPROACH_SPEED);
        }
    }

    private static BlockPos pickSpawnPos(ServerWorld world, ServerPlayerEntity player) {
        BlockPos rawPos = player.getBlockPos().add(
                world.getRandom().nextBetween(8, 14) * (world.getRandom().nextBoolean() ? 1 : -1),
                0,
                world.getRandom().nextBetween(8, 14) * (world.getRandom().nextBoolean() ? 1 : -1));
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, rawPos.getX(), rawPos.getZ());
        return new BlockPos(rawPos.getX(), y, rawPos.getZ());
    }

    private static boolean isStaringAtWatcher(ServerPlayerEntity player, EndermanEntity watcher) {
        if (player.squaredDistanceTo(watcher) > MAX_STARE_DISTANCE * MAX_STARE_DISTANCE) {
            return false;
        }
        if (!player.canSee(watcher)) {
            return false;
        }

        Vec3d look = player.getRotationVec(1.0f).normalize();
        Vec3d toWatcher = watcher.getEyePos().subtract(player.getEyePos()).normalize();
        return look.dotProduct(toWatcher) >= STARE_ALIGNMENT;
    }

    private void despawnWatcher(ServerWorld world) {
        UUID watcherId = watcherIds.remove(world.getRegistryKey());
        if (watcherId == null) {
            return;
        }

        Entity watcher = world.getEntity(watcherId);
        if (watcher == null) {
            return;
        }

        world.spawnParticles(ParticleTypes.SMOKE,
                watcher.getX(),
                watcher.getY() + 1.0,
                watcher.getZ(),
                24,
                0.4,
                0.7,
                0.4,
                0.01);
        watcher.discard();
    }
}