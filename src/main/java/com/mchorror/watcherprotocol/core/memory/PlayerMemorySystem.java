package com.mchorror.watcherprotocol.core.memory;

import com.mchorror.watcherprotocol.Watcher_protocol;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.item.BlockItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

public final class PlayerMemorySystem {
	private static final Map<UUID, PlayerPatternMemory> PLAYER_MEMORY = new HashMap<>();
	private static final Map<UUID, Vec3d> LAST_POSITIONS = new HashMap<>();
	private static final Map<UUID, Integer> LAST_SCREEN_HANDLER_SYNC = new HashMap<>();

	private PlayerMemorySystem() {
	}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(PlayerMemorySystem::onWorldTick);
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				recordScreenHandler(player);
			}
		});
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			PlayerPatternMemory memory = getMemory(player);
			if (memory != null) {
				memory.recordMining(state.getBlock(), world.getTime());
			}
		});
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (player.getStackInHand(hand).getItem() instanceof BlockItem) {
				PlayerPatternMemory memory = getMemory(player);
			if (memory != null) {
				memory.recordPlacement(hitResult.getBlockPos().offset(hitResult.getSide()));
			}
			}
			return ActionResult.PASS;
		});
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world instanceof ServerWorld) {
				PlayerPatternMemory memory = getMemory(player);
				if (memory != null) {
					memory.recordInteraction("use:" + entity.getType().toString());
				}
			}
			return ActionResult.PASS;
		});
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (world instanceof ServerWorld) {
				PlayerPatternMemory memory = getMemory(player);
				if (memory != null) {
					memory.recordInteraction("attack:" + entity.getType().toString());
				}
			}
			return ActionResult.PASS;
		});
	}

	private static void onWorldTick(ServerWorld world) {
		for (ServerPlayerEntity player : world.getPlayers()) {
			Vec3d current = player.getPos();
			Vec3d last = LAST_POSITIONS.put(player.getUuid(), current);
			if (last != null && current.squaredDistanceTo(last) > 0.0025) {
				getMemory(player).recordMovement(current, world.getTime());
			}
		}
	}

	private static void recordScreenHandler(ServerPlayerEntity player) {
		PlayerPatternMemory memory = getMemory(player);
		int currentSyncId = player.currentScreenHandler.syncId;
		Integer lastSyncId = LAST_SCREEN_HANDLER_SYNC.put(player.getUuid(), currentSyncId);
		if (lastSyncId != null && currentSyncId != 0 && currentSyncId != lastSyncId) {
			memory.recordScreenOpen();
		}
	}

	private static PlayerPatternMemory getMemory(net.minecraft.entity.player.PlayerEntity player) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)) {
			return null;
		}
		return PLAYER_MEMORY.computeIfAbsent(serverPlayer.getUuid(), id -> new PlayerPatternMemory());
	}

	private static PlayerPatternMemory getMemory(ServerPlayerEntity player) {
		return PLAYER_MEMORY.computeIfAbsent(player.getUuid(), id -> new PlayerPatternMemory());
	}

	public static PlayerPatternMemory getMemory(UUID playerId) {
		return PLAYER_MEMORY.get(playerId);
	}

	public static boolean hasMemory(UUID playerId) {
		return PLAYER_MEMORY.containsKey(playerId);
	}

	public static boolean isActive() {
		return Watcher_protocol.PHASE_CONTROLLER.getActivePhaseType() != null;
	}
}
