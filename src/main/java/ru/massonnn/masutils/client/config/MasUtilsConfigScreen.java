package ru.massonnn.masutils.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class MasUtilsConfigScreen extends Screen {
    private static final Logger logger = LoggerFactory.getLogger(MasUtilsConfigScreen.class);

    private final Screen parent;
    private MasUtilsConfig config;
    private int categoryIndex;
    private TextFieldWidget messageField;
    private int renderCount = 0;
    private int tickCount = 0;

    private static final List<String> CATEGORY_IDS = Arrays.asList(
            "general", "mineshaft", "mineshaft_party", "mineshaft_esp", "dev", "nucleus_runs");
    private static final List<Text> CATEGORY_NAMES = Arrays.asList(
            Text.literal("General"),
            Text.literal("Mineshaft"),
            Text.literal("Mineshaft Party"),
            Text.literal("Mineshaft ESP"),
            Text.literal("Dev"),
            Text.literal("Nucleus Runs"));
    private static final Color[] ESP_COLORS = {
            Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED, Color.MAGENTA, Color.WHITE, Color.ORANGE
    };
    private static final String[] ESP_COLOR_NAMES = { "Cyan", "Green", "Yellow", "Red", "Magenta", "White", "Orange" };

    public MasUtilsConfigScreen(Screen parent) {
        super(Text.literal("MasUtils Config"));
        this.parent = parent;
        logger.info("MasUtilsConfigScreen object created");
    }

    @Override
    protected void init() {
        logger.info("MasUtilsConfigScreen.init() start. width={}, height={}", width, height);

        try {
            if (config == null) {
                config = MasUtilsConfigManager.get();
            }

            this.clearChildren();
            int y = 24;
            final int buttonHeight = 20;
            final int gap = 24;
            final int col = this.width / 2 - 100;

            this.addDrawableChild(CyclingButtonWidget.<Integer>builder(i -> CATEGORY_NAMES.get(i))
                    .values(List.of(0, 1, 2, 3, 4, 5))
                    .initially(categoryIndex)
                    .build(col, y, 200, buttonHeight, Text.literal("Category"), (btn, value) -> {
                        categoryIndex = value;
                        this.init(this.client, this.width, this.height);
                    }));
            y += gap + 4;

            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, btn -> {
                this.close();
            }).dimensions(col, this.height - 28, 200, buttonHeight).build());

            String cat = CATEGORY_IDS.get(categoryIndex);
            switch (cat) {
                case "general" -> addGeneralOptions(col, y, buttonHeight, gap);
                case "mineshaft" -> addMineshaftOptions(col, y, buttonHeight, gap);
                case "mineshaft_party" -> addMineshaftPartyOptions(col, y, buttonHeight, gap);
                case "mineshaft_esp" -> addMineshaftESPOptions(col, y, buttonHeight, gap);
                case "dev" -> addDevOptions(col, y, buttonHeight, gap);
                case "nucleus_runs" -> addNucleusRunsOptions(col, y, buttonHeight, gap);
            }
            logger.info("MasUtilsConfigScreen.init() finished");
        } catch (Exception e) {
            logger.error("Error in MasUtilsConfigScreen.init()", e);
        }
    }

    private void addGeneralOptions(int x, int y, int h, int gap) {
        addDrawableChild(
                toggle(x, y, h, "Master switch", config.general.masterSwitch, v -> config.general.masterSwitch = v));
        y += gap;
        addDrawableChild(
                toggle(x, y, h, "Party commands", config.general.partyCommands, v -> config.general.partyCommands = v));
        y += gap;
        addDrawableChild(toggle(x, y, h, "Check for updates", config.general.checkForUpdates,
                v -> config.general.checkForUpdates = v));
        y += gap;
        addDrawableChild(toggle(x, y, h, "Update notifications", config.general.updateNotifications,
                v -> config.general.updateNotifications = v));
    }

    private void addMineshaftOptions(int x, int y, int h, int gap) {
        addDrawableChild(toggle(x, y, h, "Mineshaft features", config.mineshaft.mineshaftFeaturesToggle,
                v -> config.mineshaft.mineshaftFeaturesToggle = v));
        y += gap;
        addDrawableChild(toggle(x, y, h, "Corpse finder", config.mineshaft.corpseFinder,
                v -> config.mineshaft.corpseFinder = v));
        y += gap;
        addDrawableChild(toggle(x, y, h, "Mineshaft profit hint", config.mineshaft.mineshaftProfitHint,
                v -> config.mineshaft.mineshaftProfitHint = v));
        y += gap;
        addDrawableChild(toggle(x, y, h, "Mineshaft commands", config.mineshaft.mineshaftCommands,
                v -> config.mineshaft.mineshaftCommands = v));
    }

    private void addMineshaftPartyOptions(int x, int y, int h, int gap) {
        addDrawableChild(toggle(x, y, h, "Mineshaft party mode", config.mineshaft.mineshaftParty.mineshaftPartyMode,
                v -> config.mineshaft.mineshaftParty.mineshaftPartyMode = v));
        y += gap;
        addDrawableChild(toggle(x, y, h, "Auto-warp to mineshaft", config.mineshaft.mineshaftParty.autoWarpToMineshaft,
                v -> config.mineshaft.mineshaftParty.autoWarpToMineshaft = v));
        y += gap;
        messageField = new TextFieldWidget(textRenderer, x, y, 200, h, Text.literal("Message"));
        messageField.setMaxLength(256);
        messageField.setText(config.mineshaft.mineshaftParty.messageOnMineshaftSpawned);
        this.addSelectableChild(messageField);
        this.addDrawableChild(messageField);
    }

    private void addMineshaftESPOptions(int x, int y, int h, int gap) {
        addDrawableChild(
                toggle(x, y, h, "Waypoint to mineshaft", config.mineshaft.mineshaftESP.createWaypointToMineshaft,
                        v -> config.mineshaft.mineshaftESP.createWaypointToMineshaft = v));
    }

    private void addDevOptions(int x, int y, int h, int gap) {
        addDrawableChild(toggle(x, y, h, "Debug mode", config.dev.debug, v -> config.dev.debug = v));
    }

    private void addNucleusRunsOptions(int x, int y, int h, int gap) {
        addDrawableChild(toggle(x, y, h, "Jungle Temple cheese", config.nucleusRuns.jungleTempleCheese,
                v -> config.nucleusRuns.jungleTempleCheese = v));
    }

    private CyclingButtonWidget<Boolean> toggle(int x, int y, int h, String label, boolean initial,
            java.util.function.Consumer<Boolean> setter) {
        return CyclingButtonWidget.onOffBuilder(Text.literal("ON"), Text.literal("OFF"))
                .initially(initial)
                .build(x, y, 200, h, Text.literal(label), (btn, value) -> setter.accept(value));
    }

    @Override
    public void tick() {
        if (tickCount == 0) {
            logger.info("MasUtilsConfigScreen.tick() FIRST CALL. currentScreen={}",
                    client != null ? client.currentScreen : "null client");
        }
        tickCount++;
    }

    @Override
    public void removed() {
        logger.info(
                "MasUtilsConfigScreen.removed() called! Screen is being closed/replaced. renderCount={}, tickCount={}",
                renderCount, tickCount);
        super.removed();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (renderCount == 0) {
            logger.info("MasUtilsConfigScreen.render() FIRST CALL. Dimensions: {}x{}", width, height);
        }
        renderCount++;

        try {
            // In 1.21.2+, renderBackground is the standard call
            super.renderBackground(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
            super.render(context, mouseX, mouseY, delta);
        } catch (Exception e) {
            if (renderCount % 100 == 1) {
                logger.error("Error in MasUtilsConfigScreen.render()", e);
            }
        }
    }

    @Override
    public void close() {
        logger.info("MasUtilsConfigScreen.close() called");
        if (messageField != null && config != null) {
            config.mineshaft.mineshaftParty.messageOnMineshaftSpawned = messageField.getText();
        }
        MasUtilsConfigManager.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
}
