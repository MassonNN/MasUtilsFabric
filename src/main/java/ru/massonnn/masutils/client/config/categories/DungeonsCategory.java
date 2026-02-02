package ru.massonnn.masutils.client.config.categories;

import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.utils.ConfigUtils;

public class DungeonsCategory {

    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        return ConfigCategory.createBuilder()
                .id(Masutils.id("config/dungeons"))
                .name(Text.translatable("masutils.config.dungeons"))
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.dungeons.starMobs"))
                                .description(Text.translatable("masutils.config.dungeons.starMobs.@Tooltip"))
                                .binding(
                                        defaults.dungeonsConfig.highlightStarredMobs,
                                        () -> config.dungeonsConfig.highlightStarredMobs,
                                        newValue -> config.dungeonsConfig.highlightStarredMobs = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build()
                )
                .build();
    }
}