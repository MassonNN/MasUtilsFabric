package ru.massonnn.masutils.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class ModMessage {
    public static final MutableText prefix = Text.literal("[MasUtils]")
            .styled(style -> {
                style.withColor(Formatting.AQUA);
                style.withBold(true);
                return style;
            });

    public static void sendModMessage(String text) {
        Objects.requireNonNull(MinecraftClient.getInstance().player).sendMessage(
                prefix.append(Text.literal(" ")).append(Text.literal(text)),
                false
        );
    }
}
