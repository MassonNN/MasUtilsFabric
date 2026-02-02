package ru.massonnn.masutils.client.config;

import net.azureaaron.dandelion.platform.ConfigType;
import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.ConfigManager;
import net.azureaaron.dandelion.systems.DandelionConfigScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.categories.*;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

public class MasUtilsConfigManager {
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("masutils.json");

    private static final ConfigManager<MasUtilsConfig> CONFIG_MANAGER = ConfigManager.create(
            MasUtilsConfig.class,
            CONFIG_FILE,
            UnaryOperator.identity());

    public static MasUtilsConfig get() {
        return CONFIG_MANAGER.instance();
    }

    public static void save() {
        CONFIG_MANAGER.save();
    }

    public static void init() {
        CONFIG_MANAGER.load();
    }

    public static Screen createGUI(@Nullable Screen parent) {
        return createGUI(parent, "");
    }

    public static Screen createGUI(Screen parent, String search) {
        return DandelionConfigScreen.create(CONFIG_MANAGER, (defaults, config, builder) -> {
            ConfigCategory general = GeneralCategory.create(defaults, config);
            ConfigCategory mineshaft = MineshaftCategory.create(defaults, config);
            ConfigCategory qol = QolCategory.create(defaults, config);
            ConfigCategory fiesta = FiestaCategory.create(defaults, config);
            ConfigCategory dev = DevCategory.create(defaults, config);
            ConfigCategory dungeons = DungeonsCategory.create(defaults, config);
            return builder.title(Text.translatable("masutils.config.title", Masutils.VERSION))
                    .category(general)
                    .category(mineshaft)
                    .category(qol)
                    .category(fiesta)
                    .category(dungeons)
                    .category(dev)
                    .search(search);
        }).generateScreen(parent, ConfigType.MOUL_CONFIG);
    }

    public static void openConfigScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        client.execute(() -> {
            Screen screen = MasUtilsConfigManager.createGUI(client.currentScreen, "");

            client.setScreen(screen);

            client.execute(() -> {
                if (client.currentScreen != screen) {
                    client.setScreen(screen);
                }
            });
        });
    }
}
