package com.mchorror.watcherprotocol.mixin.phase1;

import com.mchorror.watcherprotocol.Watcher_protocol;
import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.phases.PhaseType;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class FurnaceTickMixin {
	private static final int MIN_COOLDOWN_TICKS = 80;
	private static int cooldownTicks = MIN_COOLDOWN_TICKS;

	@Inject(method = "tick", at = @At("HEAD"))
	private static void watcherProtocol$phaseOneFurnaceFluctuation(World world,
			net.minecraft.util.math.BlockPos pos,
			net.minecraft.block.BlockState state,
			AbstractFurnaceBlockEntity blockEntity,
			CallbackInfo info) {
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

		cooldownTicks = MIN_COOLDOWN_TICKS + serverWorld.getRandom().nextBetween(0, 120);
		if (serverWorld.getRandom().nextFloat() > 0.12f) {
			return;
		}

		AbstractFurnaceAccessor accessor = (AbstractFurnaceAccessor) blockEntity;
		accessor.watcherProtocol$setCookTime(Math.max(0, accessor.watcherProtocol$getCookTime() - 1));
	}

}
