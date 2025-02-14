package ru.massonnn.masutils.client.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.text.Text;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.utils.ConfigUtils;

import java.awt.*;

public class Mineshaft {
    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("masutils.config.mineshaft"))
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.toggle"))
                                .description(OptionDescription.of(
                                        Text.translatable("masutils.config.mineshaft.toggle.@Tooltip")
                                ))
                                .controller(ConfigUtils::createBooleanController)
                                .binding(
                                        defaults.mineshaft.mineshaftFeaturesToggle,
                                        () -> config.mineshaft.mineshaftFeaturesToggle,
                                        newValue -> config.mineshaft.mineshaftFeaturesToggle = newValue
                                )
                                .build()
                )
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.corpseFinder"))
                                .description(OptionDescription.of(
                                        Text.translatable("masutils.config.mineshaft.corpseFinder.@Tooltip")
                                ))
                                .controller(ConfigUtils::createBooleanController)
                                .binding(
                                        defaults.mineshaft.corpseFinder,
                                        () -> config.mineshaft.corpseFinder,
                                        newValue -> config.mineshaft.corpseFinder = newValue
                                ).build()
                )
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.profitHint"))
                                .controller(ConfigUtils::createBooleanController)
                                .binding(
                                        defaults.mineshaft.mineshaftProfitHint,
                                        () -> config.mineshaft.mineshaftProfitHint,
                                        newValue -> config.mineshaft.mineshaftProfitHint = newValue
                                ).build()
                )
                .group(
                        OptionGroup.createBuilder()
                                .name(Text.translatable("masutils.config.mineshaft.party"))
                                .collapsed(true)
                                .option(
                                        Option.<Boolean>createBuilder()
                                                .name(Text.translatable("masutils.config.mineshaft.party.mode"))
                                                .description(
                                                    OptionDescription.of(
                                                        Text.translatable("masutils.config.mineshaft.party.mode.@Tooltip")
                                                    )
                                                )
                                                .binding(
                                                    defaults.mineshaft.mineshaftParty.mineshaftPartyMode,
                                                    () -> config.mineshaft.mineshaftParty.mineshaftPartyMode,
                                                    newValue -> config.mineshaft.mineshaftParty.mineshaftPartyMode = newValue
                                                )
                                                .controller(ConfigUtils::createBooleanController)
                                                .build()
                                )
                                .option(
                                        Option.<Boolean>createBuilder()
                                                .name(Text.translatable("masutils.config.mineshaft.party.autowarp"))
                                                .description(
                                                        OptionDescription.of(
                                                                Text.translatable("masutils.config.mineshaft.party.autowarp.@Tooltip")
                                                        )
                                                )
                                                .binding(
                                                        defaults.mineshaft.mineshaftParty.autoWarpToMineshaft,
                                                        () -> config.mineshaft.mineshaftParty.autoWarpToMineshaft,
                                                        newValue -> config.mineshaft.mineshaftParty.autoWarpToMineshaft = newValue
                                                )
                                                .controller(ConfigUtils::createBooleanController)
                                                .build()
                                )
                                .option(
                                        Option.<Boolean>createBuilder()
                                                .name(Text.translatable("masutils.config.mineshaft.party.autotransfer"))
                                                .description(
                                                        OptionDescription.of(
                                                                Text.translatable("masutils.config.mineshaft.party.autotransfer.@Tooltip")
                                                        )
                                                )
                                                .binding(
                                                        defaults.mineshaft.mineshaftParty.autoTransferPartyOnMineshaft,
                                                        () -> config.mineshaft.mineshaftParty.autoTransferPartyOnMineshaft,
                                                        newValue -> config.mineshaft.mineshaftParty.autoTransferPartyOnMineshaft = newValue
                                                )
                                                .controller(ConfigUtils::createBooleanController)
                                                .build()
                                )
                                .option(
                                        Option.<String>createBuilder()
                                                .name(Text.translatable("masutils.config.mineshaft.party.messageon"))
                                                .description(
                                                        OptionDescription.of(
                                                                Text.translatable("masutils.config.mineshaft.party.messageon.@Tooltip")
                                                        )
                                                )
                                                .binding(
                                                        defaults.mineshaft.mineshaftParty.messageOnMineshaftSpawned,
                                                        () -> config.mineshaft.mineshaftParty.messageOnMineshaftSpawned,
                                                        newValue -> config.mineshaft.mineshaftParty.messageOnMineshaftSpawned = newValue
                                                )
                                                .controller(StringControllerBuilder::create)
                                                .build()
                                )
                                .build()
                )
                .group(OptionGroup.createBuilder()
                        .name(Text.translatable("masutils.config.mineshaft.esp"))
                        .option(
                                Option.<Boolean>createBuilder()
                                        .name(Text.translatable("masutils.config.mineshaft.esp.toggle"))
                                        .description(OptionDescription.of(Text.translatable("masutils.config.mineshaft.esp.toggle.@Tooltip")))
                                        .binding(
                                                defaults.mineshaft.mineshaftESP.createWaypointToMineshaft,
                                                () -> config.mineshaft.mineshaftESP.createWaypointToMineshaft,
                                                newValue -> config.mineshaft.mineshaftESP.createWaypointToMineshaft = newValue
                                        )
                                        .controller(ConfigUtils::createBooleanController)
                                        .build()
                        )
                        .option(
                                Option.<Color>createBuilder()
                                        .name(Text.translatable("masutils.config.mineshaft.esp.color"))
                                        .description(OptionDescription.of(Text.translatable("masutils.config.mineshaft.esp.color.@Tooltip")))
                                        .binding(
                                                defaults.mineshaft.mineshaftESP.mineshaftESPColor,
                                                () -> config.mineshaft.mineshaftESP.mineshaftESPColor,
                                                newValue -> config.mineshaft.mineshaftESP.mineshaftESPColor = newValue // TODO: add callback
                                        )
                                        .controller(opt -> ColorControllerBuilder.create(opt).allowAlpha(true))
                                        .build()
                        )
                        .build())
                .build()
        ;
    }
}
