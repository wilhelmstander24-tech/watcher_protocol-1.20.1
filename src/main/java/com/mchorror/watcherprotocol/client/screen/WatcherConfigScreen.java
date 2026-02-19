package com.mchorror.watcherprotocol.client.screen;

import com.mchorror.watcherprotocol.Watcher_protocol;
import com.mchorror.watcherprotocol.config.WatcherConfigManager;
import com.mchorror.watcherprotocol.config.WatcherProtocolConfig;
import java.util.List;
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
    private static final Text INGAME_ACCESS_LABEL = Text.translatable("watcher_protocol.config.ingame_access");
    private static final Text PHASE_ONE_LABEL = Text.translatable("watcher_protocol.config.phase_one_enabled");
    private static final Text PHASE_TWO_LABEL = Text.translatable("watcher_protocol.config.phase_two_enabled");
    private static final Text PHASE_THREE_LABEL = Text.translatable("watcher_protocol.config.phase_three_enabled");
    private static final Text PHASE_FOUR_LABEL = Text.translatable("watcher_protocol.config.phase_four_enabled");
    private static final Text PHASE_FIVE_LABEL = Text.translatable("watcher_protocol.config.phase_five_enabled");
    private static final Text PHASE_SIX_LABEL = Text.translatable("watcher_protocol.config.phase_six_enabled");
    private static final Text INTERFERENCE_LABEL = Text.translatable("watcher_protocol.config.interference_intensity");
    private static final Text INTERRUPTION_LABEL = Text.translatable("watcher_protocol.config.interruption_frequency");
    private static final Text DESTRUCTIVENESS_LABEL = Text.translatable("watcher_protocol.config.world_destructiveness");

    private static final List<Double> INTERFERENCE_VALUES = List.of(0.0, 0.5, 1.0, 1.5, 2.0);
    private static final List<Double> FREQUENCY_VALUES = List.of(0.25, 0.5, 1.0, 1.5, 2.0);
    private static final List<Double> DESTRUCTIVE_VALUES = List.of(0.0, 0.25, 0.5, 0.75, 1.0);

    private final Screen parent;
    private WatcherProtocolConfig configSnapshot;

    public WatcherConfigScreen(Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    @Override
    protected void init() {
        configSnapshot = copyConfig(WatcherConfigManager.getConfig());

        int centerX = width / 2;
        int startY = 28;
        int leftX = centerX - 102;
        int rightX = centerX + 2;
        int rowHeight = 22;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isModEnabled())
                .build(leftX, startY, 200, 20, MOD_ENABLED_LABEL, (button, value) -> configSnapshot.setModEnabled(value)));
        startY += rowHeight;
        addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isAllowInGameConfig())
                .build(leftX, startY, 200, 20, INGAME_ACCESS_LABEL,
                        (button, value) -> configSnapshot.setAllowInGameConfig(value)));
        startY += rowHeight + 2;

        addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isPhaseOneEnabled())
                .build(leftX, startY, 98, 20, PHASE_ONE_LABEL, (button, value) -> configSnapshot.setPhaseOneEnabled(value)));
        addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isPhaseTwoEnabled())
                .build(rightX, startY, 98, 20, PHASE_TWO_LABEL, (button, value) -> configSnapshot.setPhaseTwoEnabled(value)));
        startY += rowHeight;
        addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isPhaseThreeEnabled()).build(leftX, startY, 98, 20,
                PHASE_THREE_LABEL, (button, value) -> configSnapshot.setPhaseThreeEnabled(value)));
        addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isPhaseFourEnabled()).build(rightX, startY, 98, 20,
                PHASE_FOUR_LABEL, (button, value) -> configSnapshot.setPhaseFourEnabled(value)));
        startY += rowHeight;
        addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isPhaseFiveEnabled())
                .build(leftX, startY, 98, 20, PHASE_FIVE_LABEL, (button, value) -> configSnapshot.setPhaseFiveEnabled(value)));
        addDrawableChild(CyclingButtonWidget.onOffBuilder(configSnapshot.isPhaseSixEnabled())
                .build(rightX, startY, 98, 20, PHASE_SIX_LABEL, (button, value) -> configSnapshot.setPhaseSixEnabled(value)));
        startY += rowHeight + 2;

        addDrawableChild(CyclingButtonWidget.builder(WatcherConfigScreen::formatLevel)
                .values(INTERFERENCE_VALUES)
                .initially(roundToPreset(configSnapshot.getInterferenceIntensity(), INTERFERENCE_VALUES))
                .build(leftX, startY, 200, 20, INTERFERENCE_LABEL,
                        (button, value) -> configSnapshot.setInterferenceIntensity(value)));
        startY += rowHeight;
        addDrawableChild(CyclingButtonWidget.builder(WatcherConfigScreen::formatLevel)
                .values(FREQUENCY_VALUES)
                .initially(roundToPreset(configSnapshot.getInterruptionFrequency(), FREQUENCY_VALUES))
                .build(leftX, startY, 200, 20, INTERRUPTION_LABEL,
                        (button, value) -> configSnapshot.setInterruptionFrequency(value)));
        startY += rowHeight;
        addDrawableChild(CyclingButtonWidget.builder(WatcherConfigScreen::formatLevel)
                .values(DESTRUCTIVE_VALUES)
                .initially(roundToPreset(configSnapshot.getWorldDestructiveness(), DESTRUCTIVE_VALUES))
                .build(leftX, startY, 200, 20, DESTRUCTIVENESS_LABEL,
                        (button, value) -> configSnapshot.setWorldDestructiveness(value)));

        int actionY = Math.max(startY + rowHeight + 6, height - 56);

        addDrawableChild(ButtonWidget.builder(SAVE_LABEL, button -> {
                    applyConfig();
                    close();
                })
                .dimensions(leftX, actionY, 98, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(CANCEL_LABEL, button -> close())
                .dimensions(rightX, actionY, 98, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, TITLE, width / 2, 10, 0xFFFFFF);

        String path = WatcherConfigManager.getConfigPath().toString();
        context.drawTextWithShadow(textRenderer,
                CONFIG_PATH_LABEL.copy().append(Text.literal(": ").formatted(Formatting.GRAY))
                        .append(Text.literal(path).formatted(Formatting.AQUA)),
                20,
                height - 30,
                0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private void applyConfig() {
        WatcherProtocolConfig config = WatcherConfigManager.getConfig();
        config.setModEnabled(configSnapshot.isModEnabled());
        config.setAllowInGameConfig(configSnapshot.isAllowInGameConfig());
        config.setPhaseOneEnabled(configSnapshot.isPhaseOneEnabled());
        config.setPhaseTwoEnabled(configSnapshot.isPhaseTwoEnabled());
        config.setPhaseThreeEnabled(configSnapshot.isPhaseThreeEnabled());
        config.setPhaseFourEnabled(configSnapshot.isPhaseFourEnabled());
        config.setPhaseFiveEnabled(configSnapshot.isPhaseFiveEnabled());
        config.setPhaseSixEnabled(configSnapshot.isPhaseSixEnabled());
        config.setInterferenceIntensity(configSnapshot.getInterferenceIntensity());
        config.setInterruptionFrequency(configSnapshot.getInterruptionFrequency());
        config.setWorldDestructiveness(configSnapshot.getWorldDestructiveness());
        WatcherConfigManager.save();
        Watcher_protocol.LOGGER.info("Saved Watcher Protocol config from ModMenu.");
    }

    private static WatcherProtocolConfig copyConfig(WatcherProtocolConfig source) {
        WatcherProtocolConfig copy = new WatcherProtocolConfig();
        copy.setModEnabled(source.isModEnabled());
        copy.setAllowInGameConfig(source.isAllowInGameConfig());
        copy.setPhaseOneEnabled(source.isPhaseOneEnabled());
        copy.setPhaseTwoEnabled(source.isPhaseTwoEnabled());
        copy.setPhaseThreeEnabled(source.isPhaseThreeEnabled());
        copy.setPhaseFourEnabled(source.isPhaseFourEnabled());
        copy.setPhaseFiveEnabled(source.isPhaseFiveEnabled());
        copy.setPhaseSixEnabled(source.isPhaseSixEnabled());
        copy.setInterferenceIntensity(source.getInterferenceIntensity());
        copy.setInterruptionFrequency(source.getInterruptionFrequency());
        copy.setWorldDestructiveness(source.getWorldDestructiveness());
        return copy;
    }

    private static Text formatLevel(Double value) {
        return Text.literal(String.format("%.2fx", value));
    }

    private static Double roundToPreset(double value, List<Double> candidates) {
        double bestDelta = Double.MAX_VALUE;
        double best = candidates.get(0);
        for (Double candidate : candidates) {
            double delta = Math.abs(candidate - value);
            if (delta < bestDelta) {
                bestDelta = delta;
                best = candidate;
            }
        }
        return best;
    }
}