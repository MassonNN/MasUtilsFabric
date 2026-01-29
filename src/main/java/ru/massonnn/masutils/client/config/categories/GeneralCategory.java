package ru.massonnn.masutils.client.config.categories;

import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.utils.ConfigUtils;

public class GeneralCategory {

    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        return ConfigCategory.createBuilder()
                .id(Masutils.id("config/general"))
                .name(Text.translatable("masutils.config.general"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("masutils.config.general.masterSwitch"))
                        .description(Text.translatable("masutils.config.general.masterSwitch.@Tooltip"))
                        .binding(defaults.general.masterSwitch,
                                () -> config.general.masterSwitch,
                                newValue -> config.general.masterSwitch = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("masutils.config.general.checkForUpdates"))
                        .description(Text.translatable("masutils.config.general.checkForUpdates.@Tooltip"))
                        .binding(defaults.general.checkForUpdates,
                                () -> config.general.checkForUpdates,
                                newValue -> config.general.checkForUpdates = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("masutils.config.general.partyCommands"))
                        .description(Text.translatable("masutils.config.general.partyCommands.@Tooltip"))
                        .binding(defaults.general.partyCommands,
                                () -> config.general.partyCommands,
                                newValue -> config.general.partyCommands = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .build();
    }
}