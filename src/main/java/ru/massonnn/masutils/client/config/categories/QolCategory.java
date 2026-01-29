package ru.massonnn.masutils.client.config.categories;

import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.controllers.BooleanController;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.utils.ConfigUtils;

public class QolCategory {

    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        return ConfigCategory.createBuilder()
                .id(Masutils.id("config/qol"))
                .name(Text.translatable("masutils.config.qol"))
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.qol.blockHeadPlacement"))
                                .description(Text.translatable("masutils.config.qol.blockHeadPlacement.@Tooltip"))
                                .binding(
                                        defaults.qol.blockHeadPlacement,
                                        () -> config.qol.blockHeadPlacement,
                                        newValue -> config.qol.blockHeadPlacement = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build()
                )
                .build();
    }
}