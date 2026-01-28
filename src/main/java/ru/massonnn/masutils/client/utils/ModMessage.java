package ru.massonnn.masutils.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class ModMessage {
    public static final Text prefix = Text.literal("[MasUtils]")
            .styled(
                    style -> style.withClickEvent(
                            new ClickEvent.RunCommand("/masutils"))
                            .withColor(Formatting.DARK_AQUA)
                            .withBold(false));

    public static void sendModMessage(Text text) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(
                    prefix.copy().append(Text.literal(" ")).append(text),
                    false);
        }
    }

    public static void sendModMessage(String text) {
        sendModMessage(Text.literal(text).formatted(Formatting.WHITE));
    }
}
