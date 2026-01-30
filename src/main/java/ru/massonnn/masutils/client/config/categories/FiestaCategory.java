package ru.massonnn.masutils.client.config.categories;

import net.azureaaron.dandelion.systems.ConfigCategory;
import net.azureaaron.dandelion.systems.Option;
import net.minecraft.text.Text;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.events.LocationEvents;
import ru.massonnn.masutils.client.features.mining.GrottoFinder;
import ru.massonnn.masutils.client.hypixel.LocationUtils;
import ru.massonnn.masutils.client.utils.ConfigUtils;

public class FiestaCategory {

    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        return ConfigCategory.createBuilder()
                .id(Masutils.id("config/fiesta"))
                .name(Text.translatable("masutils.config.fiesta"))
                .option(
                        Option.<Boolean>createBuilder()
                                .name(Text.translatable("masutils.config.fiesta.grottoFinder"))
                                .description(Text.translatable("masutils.config.fiesta.grottoFinder.@Tooltip"))
                                .binding(
                                        defaults.fiestaConfig.grottoFinder,
                                        () -> config.fiestaConfig.grottoFinder,
                                        (newValue) -> {
                                            config.fiestaConfig.grottoFinder = newValue;
                                            if (LocationUtils.isInCrystalHollows()) {
                                                if (newValue) GrottoFinder.startScanning();
                                                else GrottoFinder.stopScanning();
                                            }
                                        })
                                .controller(ConfigUtils.createBooleanController())
                                .build()
                )
                .build();
    }
}