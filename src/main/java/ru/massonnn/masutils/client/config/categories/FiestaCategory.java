package ru.massonnn.masutils.client.config.categories;

import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.features.mining.GrottoFinder;
import ru.massonnn.masutils.client.hypixel.LocationUtils;
import ru.massonnn.masutils.client.utils.ConfigUtils;

public class FiestaCategory {

    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        return ConfigCategory.createBuilder()
                .id(Masutils.id("config/crystalhollows"))
                .name(Text.translatable("masutils.config.crystalHollowsFinder"))
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.crystalHollowsFinder.grottoFinder"))
                                .description(Text.translatable("masutils.config.fiesta.crystalHollowsFinder.@Tooltip"))
                                .binding(
                                        defaults.crystalHollowsFinder.grottoFinder,
                                        () -> config.crystalHollowsFinder.grottoFinder,
                                        (newValue) -> {
                                            config.crystalHollowsFinder.grottoFinder = newValue;
                                            if (LocationUtils.isInCrystalHollows()) {
                                                if (newValue) GrottoFinder.startScanning();
                                                else GrottoFinder.stopScanning();
                                            }
                                        })
                                .controller(ConfigUtils.createBooleanController())
                                .build()
                )
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.crystalHollowsFinder.lostVisitorsFinder"))
                                .description(Text.translatable("masutils.config.crystalHollowsFinder.lostVisitorsFinder.@Tooltip"))
                                .binding(
                                        defaults.crystalHollowsFinder.lostVisitorsFinder,
                                        () -> config.crystalHollowsFinder.lostVisitorsFinder,
                                        (newValue) -> {
                                            config.crystalHollowsFinder.lostVisitorsFinder = newValue;
                                        })
                                .controller(ConfigUtils.createBooleanController())
                                .build()
                )
                .build();
    }
}