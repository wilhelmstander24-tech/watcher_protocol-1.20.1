package com.mchorror.watcherprotocol.mixin.phase1;

import com.mchorror.watcherprotocol.Watcher_protocol;
import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.phases.PhaseType;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireMixin {
	private static final int MIN_COOLDOWN_TICKS = 120;
	private static int cooldownTicks = MIN_COOLDOWN_TICKS;

	@Inject(method = "getReceivedRedstonePower", at = @At("RETURN"), cancellable = true)
	private void watcherProtocol$phaseOneRedstoneDisturbance(World world,
			net.minecraft.util.math.BlockPos pos,
			CallbackInfoReturnable<Integer> info) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		if (!WatcherConfigManager.getConfig().isModEnabled()
				|| !WatcherConfigManager.getConfig().isPhaseOneEnabled()) {
			return;
		}

		if (Watcher_protocol.PHASE_CONTROLLER.getActivePhaseType() != PhaseType.PHASE_1) {
			return;
		}

		if (--cooldownTicks > 0) {
			return;
		}

		cooldownTicks = MIN_COOLDOWN_TICKS + serverWorld.getRandom().nextBetween(0, 160);
		if (serverWorld.getRandom().nextFloat() > 0.08f) {
			return;
		}

		int power = info.getReturnValue();
		if (power <= 0) {
			return;
		}

		info.setReturnValue(Math.max(0, power - 1));
	}
}
