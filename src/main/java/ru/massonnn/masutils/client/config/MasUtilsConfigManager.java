package ru.massonnn.masutils.client.config;

import net.azureaaron.dandelion.platform.ConfigType;
import net.azureaaron.dandelion.systems.ConfigManager;
import net.azureaaron.dandelion.systems.DandelionConfigScreen;
import net.minecraft.client.MinecraftClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.categories.GeneralCategory;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.UnaryOperator;

public class MasUtilsConfigManager {
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("masutils.json");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Color.class, new ColorSerializer())
            .create();

    private static MasUtilsConfig instance;
    private static final ConfigManager<MasUtilsConfig> CONFIG_MANAGER = ConfigManager.create(MasUtilsConfig.class, CONFIG_FILE, UnaryOperator.identity());

    public static MasUtilsConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            Files.writeString(CONFIG_FILE, GSON.toJson(instance));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }

    public void init() {
        load();
    }

    private static void load() {
        if (Files.exists(CONFIG_FILE)) {
            try {
                instance = GSON.fromJson(Files.readString(CONFIG_FILE), MasUtilsConfig.class);
                if (instance == null) {
                    instance = new MasUtilsConfig();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config", e);
            }
        } else {
            instance = new MasUtilsConfig();
            save();
        }
    }

    public static Screen createGUI(Screen parent) {
        return createGUI(parent, "");
    }

//    public static Screen createGUI(Screen parent) {
//        return new MasUtilsConfigScreen(parent);
//    }

    public static Screen createGUI(@Nullable Screen parent, String search) {
        return DandelionConfigScreen.create(CONFIG_MANAGER, (defaults, config, builder) -> builder
                .title(Text.translatable("masutils.config.title", Masutils.VERSION))
                .category(GeneralCategory.create(defaults, config))
                .search(search)
        ).generateScreen(parent, ConfigType.MOUL_CONFIG);
    }

    public static void openConfigScreen() {
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().currentScreen != null) {
            MinecraftClient.getInstance().execute(() -> {
                MinecraftClient.getInstance().setScreen(createGUI(MinecraftClient.getInstance().currentScreen));
            });
        }
    }

    private static class ColorSerializer implements JsonSerializer<Color>, JsonDeserializer<Color> {
        @Override
        public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("r", src.getRed() / 255.0);
            obj.addProperty("g", src.getGreen() / 255.0);
            obj.addProperty("b", src.getBlue() / 255.0);
            obj.addProperty("a", src.getAlpha() / 255.0);
            return obj;
        }

        @Override
        public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                double r = obj.get("r").getAsDouble();
                double g = obj.get("g").getAsDouble();
                double b = obj.get("b").getAsDouble();
                double a = obj.has("a") ? obj.get("a").getAsDouble() : 1.0;
                return new Color(
                        (int) (r * 255),
                        (int) (g * 255),
                        (int) (b * 255),
                        (int) (a * 255));
            } else if (json.isJsonPrimitive()) {
                return new Color(json.getAsInt(), true);
            } else {
                throw new JsonParseException("Expected JSON object or number for Color, got: " + json);
            }
        }
    }
}
