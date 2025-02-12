package ru.massonnn.masutils.client.config.categories;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import net.minecraft.text.Text;
import ru.massonnn.masutils.client.config.MasUtilsConfig;
import ru.massonnn.masutils.client.utils.ConfigUtils;

public class General {
    public static ConfigCategory create(MasUtilsConfig defaults, MasUtilsConfig config) {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("masutils.config.general"))
                .option(
                        Option.<Boolean>createBuilder()
                            .name(Text.translatable("masutils.config.general.updateNotifications"))
                            .binding(
                                    defaults.general.updateNotifications,
                                    () -> config.general.updateNotifications,
                                    newValue -> config.general.updateNotifications = newValue
                            )
                            .controller(ConfigUtils::createBooleanController)
                            .build()
                ).build()
        ;
    }
}
