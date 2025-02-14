package ru.massonnn.masutils.client.config;

import com.google.gson.FieldNamingPolicy;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.categories.General;
import ru.massonnn.masutils.client.config.categories.Mineshaft;

import java.nio.file.Path;

public class MasUtilsConfigManager {
    private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("masutils.json");
    private static final ConfigClassHandler<MasUtilsConfig> HANDLER = ConfigClassHandler.createBuilder(MasUtilsConfig.class)
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(CONFIG_FILE)
                    .setJson5(false)
                    .appendGsonBuilder(builder -> builder
                            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                            .registerTypeHierarchyAdapter(Identifier.class, new Identifier.Serializer()))
                    .build())
            .build();

    public static MasUtilsConfig get() {
        return HANDLER.instance();
    }

    public static void save() {
        HANDLER.save();
    }

    public void init() {
        if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass() != Masutils.class) {
            throw new RuntimeException("Called config init from an illegal place!");
        }
        HANDLER.load();
    }

    public static Screen createGUI(Screen parent) {
        return YetAnotherConfigLib.create(HANDLER, (defaults, config, builder) -> {
            builder.title(Text.translatable("masutils.config.title"))
                    .category(General.create(defaults, config))
                    .category(Mineshaft.create(defaults, config));
            return builder;
        }).generateScreen(parent);
    }

}
