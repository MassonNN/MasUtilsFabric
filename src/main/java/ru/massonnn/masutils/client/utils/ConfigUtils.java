package ru.massonnn.masutils.client.utils;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.ValueFormatter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;

public class ConfigUtils {
    public static final ValueFormatter<Formatting> FORMATTING_FORMATTER = formatting -> Text.literal(StringUtils.capitalize(formatting.getName().replaceAll("_", " ")));
    public static final ValueFormatter<Float> FLOAT_TWO_FORMATTER = value -> Text.literal(String.format("%,.2f", value).replaceAll("[\u00a0\u202F]", " "));

    public static BooleanControllerBuilder createBooleanController(Option<Boolean> opt) {
        return BooleanControllerBuilder.create(opt).yesNoFormatter().coloured(true);
    }

    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> EnumControllerBuilder<E> createEnumCyclingListController(Option<E> opt) {
        return EnumControllerBuilder.create(opt).enumClass((Class<E>) opt.stateManager().get().getClass());
    }

}
