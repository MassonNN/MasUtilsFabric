package ru.massonnn.masutils.client.config.categories;

import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.utils.ConfigUtils;

public class DevCategory {

    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        return ConfigCategory.createBuilder()
                .id(Masutils.id("config/dev"))
                .name(Text.translatable("masutils.config.dev"))
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.dev.debug"))
                                .description(Text.translatable("masutils.config.dev.debug.@Tooltip"))
                                .binding(
                                        defaults.dev.debug,
                                        () -> config.dev.debug,
                                        newValue -> config.qol.blockHeadPlacement = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build()
                )
                .build();
    }
}