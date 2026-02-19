package com.mchorror.watcherprotocol.phases.phase1;

import com.mchorror.watcherprotocol.Watcher_protocol;
import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.core.MentalMeltdownSystem;
import com.mchorror.watcherprotocol.phases.PhaseType;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class MobDisruptionSystem {
	private static final double PANIC_RADIUS = 16.0;
	private static final int AI_PULSE_INTERVAL = 20;

	private static final Map<net.minecraft.registry.RegistryKey<World>, Integer> worldPulseCooldown = new HashMap<>();

	private MobDisruptionSystem() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(MobDisruptionSystem::onWorldTick);
		AttackEntityCallback.EVENT.register(MobDisruptionSystem::onEntityAttackedByPlayer);
		UseEntityCallback.EVENT.register(MobDisruptionSystem::onUseEntity);
	}

	private static void onWorldTick(ServerWorld world) {
		if (!isPhaseOneActive()) {
			return;
		}

		int cooldown = worldPulseCooldown.getOrDefault(world.getRegistryKey(), AI_PULSE_INTERVAL) - 1;
		if (cooldown > 0) {
			worldPulseCooldown.put(world.getRegistryKey(), cooldown);
			return;
		}
		worldPulseCooldown.put(world.getRegistryKey(), AI_PULSE_INTERVAL);

		for (Entity entity : world.iterateEntities()) {
			if (entity instanceof MobEntity mob) {
				randomizeMobBehavior(world, mob);
			}
		}
	}

	private static ActionResult onEntityAttackedByPlayer(PlayerEntity player, World world, Hand hand, Entity entity,
			net.minecraft.util.hit.EntityHitResult hitResult) {
		if (!(world instanceof ServerWorld serverWorld) || !isPhaseOneActive()) {
			return ActionResult.PASS;
		}

		double threatMultiplier = getMeltdownThreatMultiplier(player);

		if (entity instanceof PassiveEntity passive && serverWorld.getRandom().nextFloat() < 0.2f * threatMultiplier) {
			player.damage(serverWorld.getDamageSources().mobAttack(passive), 1.0f);
			passive.setTarget(player);
		}

		if (entity instanceof Angerable angerable && serverWorld.getRandom().nextFloat() < 0.35f * threatMultiplier) {
			angerable.setAngryAt(player.getUuid());
			if (entity instanceof MobEntity mob) {
				mob.setTarget(player);
			}
		}

		return ActionResult.PASS;
	}

	private static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity,
			net.minecraft.util.hit.EntityHitResult hitResult) {
		if (!(world instanceof ServerWorld serverWorld) || !isPhaseOneActive()) {
			return ActionResult.PASS;
		}

		if (entity instanceof VillagerEntity villager && serverWorld.getRandom().nextFloat() < 0.15f) {
			player.sendMessage(Text.translatable("watcher_protocol.phase1.villager_refuse"), true);
			villager.getNavigation().stop();
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	private static void randomizeMobBehavior(ServerWorld world, MobEntity mob) {
		if (mob instanceof AnimalEntity animal && world.getRandom().nextFloat() < 0.04f) {
			panicNearbyHerd(world, animal);
		}

		if (mob instanceof HostileEntity hostile) {
			double threatMultiplier = getMeltdownThreatMultiplier(world.getClosestPlayer(mob, 16.0));
			if (world.getRandom().nextFloat() < 0.05f / (float) threatMultiplier) {
				hostile.setTarget(null);
				hostile.getNavigation().stop();
			}
			if (world.getRandom().nextFloat() < 0.03f / (float) threatMultiplier) {
				runAwayFromClosestPlayer(world, hostile, 1.2);
			}
			if (world.getRandom().nextFloat() < 0.04f * threatMultiplier) {
				PlayerEntity target = world.getClosestPlayer(mob, 16.0);
				if (target != null) {
					hostile.setTarget(target);
				}
			}
		}

		if (mob instanceof VillagerEntity villager) {
			if (world.getRandom().nextFloat() < 0.06f) {
				villager.getNavigation().stop();
			}
			if (world.getRandom().nextFloat() < 0.04f) {
				double yaw = villager.getBodyYaw() + 180.0;
				villager.setYaw((float) yaw);
				villager.setHeadYaw((float) yaw);
			}
			if (world.getRandom().nextFloat() < 0.03f) {
				runAwayFromClosestPlayer(world, villager, 0.9);
			}
		}

		if (mob instanceof Angerable angerable && world.getRandom().nextFloat() < 0.05f) {
			PlayerEntity target = world.getClosestPlayer(mob, 12.0);
			if (target != null) {
				angerable.setAngryAt(target.getUuid());
				mob.setTarget(target);
			}
		}
	}

	private static void panicNearbyHerd(ServerWorld world, AnimalEntity origin) {
		var herd = world.getEntitiesByClass(AnimalEntity.class,
				origin.getBoundingBox().expand(PANIC_RADIUS),
				other -> other != origin);
		for (AnimalEntity member : herd) {
			Vec3d push = member.getPos().subtract(origin.getPos()).normalize().multiply(0.25);
			member.setVelocity(member.getVelocity().add(push.x, 0.02, push.z));
			member.getNavigation().startMovingTo(member.getX() + push.x * 10.0, member.getY(), member.getZ() + push.z * 10.0, 1.25);
		}
	}

	private static void runAwayFromClosestPlayer(ServerWorld world, MobEntity mob, double speed) {
		PlayerEntity nearest = world.getClosestPlayer(mob, 16.0);
		if (nearest == null) {
			return;
		}

		Vec3d away = mob.getPos().subtract(nearest.getPos());
		if (away.lengthSquared() < 1.0E-4) {
			away = new Vec3d(world.getRandom().nextDouble() - 0.5, 0.0, world.getRandom().nextDouble() - 0.5);
		}
		away = away.normalize().multiply(8.0);
		mob.getNavigation().startMovingTo(mob.getX() + away.x, mob.getY(), mob.getZ() + away.z, speed);
	}

	private static double getMeltdownThreatMultiplier(PlayerEntity player) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)) {
			return 1.0;
		}

		float meltdown = MentalMeltdownSystem.getMeltdown(serverPlayer);
		if (meltdown < 20.0f) {
			return 2.0;
		}
		if (meltdown < 40.0f) {
			return 1.6;
		}
		if (meltdown < 60.0f) {
			return 1.25;
		}
		return 1.0;
	}

	private static boolean isPhaseOneActive() {
		return WatcherConfigManager.getConfig().isModEnabled()
				&& WatcherConfigManager.getConfig().isPhaseOneEnabled()
				&& Watcher_protocol.PHASE_CONTROLLER.getActivePhaseType() == PhaseType.PHASE_1;
	}
}
