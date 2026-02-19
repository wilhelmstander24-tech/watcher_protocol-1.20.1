package com.mchorror.watcherprotocol.client.screen;

import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.config.WatcherProtocolConfig;
import com.mchorror.watcherprotocol.Watcher_protocol;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WatcherConfigScreen extends Screen {
	private static final Text TITLE = Text.translatable("watcher_protocol.config.title");
	private static final Text CONFIG_PATH_LABEL = Text.translatable("watcher_protocol.config.path");
	private static final Text SAVE_LABEL = Text.translatable("watcher_protocol.config.save");
	private static final Text CANCEL_LABEL = Text.translatable("watcher_protocol.config.cancel");
	private static final Text MOD_ENABLED_LABEL = Text.translatable("watcher_protocol.config.mod_enabled");
	private static final Text PHASE_ONE_LABEL = Text.translatable("watcher_protocol.config.phase_one_enabled");
	private static final Text INGAME_ACCESS_LABEL = Text.translatable("watcher_protocol.config.ingame_access");

	private final Screen parent;
	private WatcherProtocolConfig configSnapshot;
	private CyclingButtonWidget<Boolean> modEnabledButton;
	private CyclingButtonWidget<Boolean> phaseOneButton;
	private CyclingButtonWidget<Boolean> inGameAccessButton;

	public WatcherConfigScreen(Screen parent) {
		super(TITLE);
		this.parent = parent;
	}

	@Override
	protected void init() {
		configSnapshot = copyConfig(WatcherConfigManager.getConfig());

		int centerX = width / 2;
		int startY = height / 4;

		modEnabledButton = addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isModEnabled())
				.build(centerX - 100, startY, 200, 20, MOD_ENABLED_LABEL,
						(button, value) -> configSnapshot.setModEnabled(value)));

		phaseOneButton = addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isPhaseOneEnabled())
				.build(centerX - 100, startY + 24, 200, 20, PHASE_ONE_LABEL,
						(button, value) -> configSnapshot.setPhaseOneEnabled(value)));

		inGameAccessButton = addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isAllowInGameConfig())
				.build(centerX - 100, startY + 48, 200, 20, INGAME_ACCESS_LABEL,
						(button, value) -> configSnapshot.setAllowInGameConfig(value)));

		addDrawableChild(ButtonWidget.builder(SAVE_LABEL, button -> {
					applyConfig();
					close();
				})
				.dimensions(centerX - 100, startY + 84, 98, 20)
				.build());

		addDrawableChild(ButtonWidget.builder(CANCEL_LABEL, button -> close())
				.dimensions(centerX + 2, startY + 84, 98, 20)
				.build());

	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		context.drawCenteredTextWithShadow(textRenderer, TITLE, width / 2, 20, 0xFFFFFF);

		String path = WatcherConfigManager.getConfigPath().toString();
		context.drawTextWithShadow(textRenderer, CONFIG_PATH_LABEL.copy().append(Text.literal(": ").formatted(Formatting.GRAY))
						.append(Text.literal(path).formatted(Formatting.AQUA)),
				20, height - 40, 0xFFFFFF);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void close() {
		client.setScreen(parent);
	}

	private void applyConfig() {
		WatcherProtocolConfig config = WatcherConfigManager.getConfig();
		config.setModEnabled(configSnapshot.isModEnabled());
		config.setPhaseOneEnabled(configSnapshot.isPhaseOneEnabled());
		config.setAllowInGameConfig(configSnapshot.isAllowInGameConfig());
		WatcherConfigManager.save();
		Watcher_protocol.LOGGER.info("Saved Watcher Protocol config from ModMenu.");
	}

	private static WatcherProtocolConfig copyConfig(WatcherProtocolConfig source) {
		WatcherProtocolConfig copy = new WatcherProtocolConfig();
		copy.setModEnabled(source.isModEnabled());
		copy.setPhaseOneEnabled(source.isPhaseOneEnabled());
		copy.setAllowInGameConfig(source.isAllowInGameConfig());
		return copy;
	}
}
