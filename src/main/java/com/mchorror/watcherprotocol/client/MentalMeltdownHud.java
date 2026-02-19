package com.mchorror.watcherprotocol.client;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public final class MentalMeltdownHud {
	private static final float MAX_VALUE = 100.0f;
	private static float localValue = MAX_VALUE;
	private static net.minecraft.util.math.Vec3d lastPos;

	private MentalMeltdownHud() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(MentalMeltdownHud::onClientTick);
		HudRenderCallback.EVENT.register(MentalMeltdownHud::onHudRender);
	}

	private static void onClientTick(MinecraftClient client) {
		if (!WatcherConfigManager.getConfig().isModEnabled()) {
			localValue = MAX_VALUE;
			lastPos = null;
			return;
		}

		ClientPlayerEntity player = client.player;
		if (player == null || client.world == null) {
			return;
		}

		boolean moving = false;
		if (lastPos != null) {
			moving = player.getPos().squaredDistanceTo(lastPos) > 0.0008;
		}
		lastPos = player.getPos();

		int timeSinceRest = player.getStatHandler().getStat(net.minecraft.stat.Stats.CUSTOM,
				net.minecraft.stat.Stats.TIME_SINCE_REST);

		float drain = 0.0f;
		if (timeSinceRest > 24000) {
			drain += Math.min(0.08f, (timeSinceRest - 24000) / 24000.0f * 0.02f + 0.01f);
		}
		if (moving && !player.isSleeping()) {
			drain += 0.03f;
		}

		float recovery = 0.0f;
		if (player.isSleeping()) {
			recovery += 0.30f;
		} else if (!moving) {
			recovery += 0.012f;
		}

		localValue = Math.max(0.0f, Math.min(MAX_VALUE, localValue - drain + recovery));
	}

	private static void onHudRender(DrawContext context, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden || !WatcherConfigManager.getConfig().isModEnabled()) {
			return;
		}

		int width = context.getScaledWindowWidth();
		int barWidth = 120;
		int barHeight = 8;
		int x = (width - barWidth) / 2;
		int y = 12;

		context.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0x90000000);
		float percent = localValue / MAX_VALUE;
		int fill = Math.max(0, Math.min(barWidth, Math.round(barWidth * percent)));
		int color = percent > 0.5f ? 0xFF4CAF50 : percent > 0.25f ? 0xFFFFB300 : 0xFFE53935;
		context.fill(x, y, x + fill, y + barHeight, color);

		context.drawTextWithShadow(client.textRenderer, Text.translatable("watcher_protocol.hud.mental_stability"), x, y - 10, 0xFFFFFFFF);

		if (percent < 0.25f) {
			int overlayAlpha = (int) (70 + 60 * (1.0f + Math.sin((client.world.getTime() + tickDelta) * 0.45f)) * 0.5f);
			int overlayColor = (overlayAlpha << 24) | 0x00FF0000;
			context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), overlayColor);
		}
	}
}
