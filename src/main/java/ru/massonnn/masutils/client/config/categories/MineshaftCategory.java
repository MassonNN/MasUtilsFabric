package ru.massonnn.masutils.client.config.categories;

import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.azureaaron.dandelion.systems.OptionGroup;
import net.azureaaron.dandelion.systems.controllers.FloatController;
import net.azureaaron.dandelion.systems.controllers.StringController;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.utils.ConfigUtils;

import java.awt.*;

public class MineshaftCategory {

    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                .id(Masutils.id("config/mineshaft"))
                .name(Text.translatable("masutils.config.mineshaft"))
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("masutils.config.mineshaft.toggle"))
                        .description(Text.translatable("masutils.config.mineshaft.toggle.@Tooltip"))
                        .binding(defaults.mineshaft.mineshaftFeaturesToggle,
                                () -> config.mineshaft.mineshaftFeaturesToggle,
                                newValue -> config.mineshaft.mineshaftFeaturesToggle = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("masutils.config.mineshaft.corpseFinder"))
                        .description(Text.translatable("masutils.config.mineshaft.corpseFinder.@Tooltip"))
                        .binding(defaults.mineshaft.corpseFinder,
                                () -> config.mineshaft.corpseFinder,
                                newValue -> config.mineshaft.corpseFinder = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build())
                .option(Option.<Boolean>createBuilder()
                        .name(Text.translatable("masutils.config.mineshaft.profitHint"))
                        .description(Text.translatable("masutils.config.mineshaft.profitHint.@Tooltip"))
                        .binding(defaults.mineshaft.mineshaftProfitHint,
                                () -> config.mineshaft.mineshaftProfitHint,
                                newValue -> config.mineshaft.mineshaftProfitHint = newValue)
                        .controller(ConfigUtils.createBooleanController())
                        .build()
            );


        categoryBuilder.groupIf(config.mineshaft.mineshaftFeaturesToggle,
                OptionGroup.createBuilder()
                        .name(Text.translatable("masutils.config.mineshaft.party"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.party.mode"))
                                .description(Text.translatable("masutils.config.mineshaft.party.mode.@Tooltip"))
                                .binding(defaults.mineshaft.mineshaftParty.mineshaftPartyMode,
                                        () -> config.mineshaft.mineshaftParty.mineshaftPartyMode,
                                        newValue -> config.mineshaft.mineshaftParty.mineshaftPartyMode = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.party.autowarp"))
                                .description(Text.translatable("masutils.config.mineshaft.party.autowarp.@Tooltip"))
                                .binding(defaults.mineshaft.mineshaftParty.autoWarpToMineshaft,
                                        () -> config.mineshaft.mineshaftParty.autoWarpToMineshaft,
                                        newValue -> config.mineshaft.mineshaftParty.autoWarpToMineshaft = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<String>createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.party.messageon"))
                                .description(Text.translatable("masutils.config.mineshaft.party.messageon.@Tooltip"))
                                .binding(defaults.mineshaft.mineshaftParty.messageOnMineshaftSpawned,
                                        () -> config.mineshaft.mineshaftParty.messageOnMineshaftSpawned,
                                        newValue -> config.mineshaft.mineshaftParty.messageOnMineshaftSpawned = newValue)
                                .controller(StringController.createBuilder().build())
                                .build())
                        .build());

        categoryBuilder.groupIf(config.mineshaft.mineshaftFeaturesToggle,
                OptionGroup.createBuilder()
                        .name(Text.translatable("masutils.config.mineshaft.esp"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.esp.toggle"))
                                .description(Text.translatable("masutils.config.mineshaft.esp.toggle.@Tooltip"))
                                .binding(defaults.mineshaft.mineshaftESP.createWaypointToMineshaft,
                                        () -> config.mineshaft.mineshaftESP.createWaypointToMineshaft,
                                        newValue -> config.mineshaft.mineshaftESP.createWaypointToMineshaft = newValue)
                                .controller(ConfigUtils.createBooleanController())
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.esp.color"))
                                .binding(defaults.mineshaft.mineshaftESP.mineshaftESPColor,
                                        () -> config.mineshaft.mineshaftESP.mineshaftESPColor,
                                        newValue -> config.mineshaft.mineshaftESP.mineshaftESPColor = newValue)
                                .controller(ConfigUtils.createColourController(true))
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.esp.traceThickness"))
                                .description(Text.translatable("masutils.config.mineshaft.esp.traceThickness.@Tooltip"))
                                .binding(defaults.mineshaft.traceThickness,
                                        () -> config.mineshaft.traceThickness,
                                        newValue -> config.mineshaft.traceThickness = newValue)
                                .controller(FloatController.createBuilder().range(1f, 50f).build())
                                .build())
                        .build());

        return categoryBuilder.build();
    }
}