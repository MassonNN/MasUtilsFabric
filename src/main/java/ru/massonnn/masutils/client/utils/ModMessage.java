package ru.massonnn.masutils.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class ModMessage {
    public static final Text prefix = Text.literal("[MasUtils]")
            .styled(
                    style -> style.withClickEvent(
                            new ClickEvent.RunCommand("/masutils"))
                            .withHoverEvent(
                                    new HoverEvent.ShowText(Text.translatable("masutils.config.openhint"))
                            )
                            .withColor(Formatting.DARK_AQUA)
                            .withBold(false));

    public static final Text error = Text.literal("[ERROR]")
            .styled(style -> style.withColor(Formatting.RED).withBold(false));

    public static void sendModMessage(Text text) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(
                    prefix.copy().append(Text.literal(" ")).append(text.copy().styled(
                                    style -> style.withColor(Formatting.WHITE).withBold(false)
                    )),
                    false);
        }
    }

    public static void sendErrorMessage(String text) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(
                    prefix.copy()
                            .append(Text.literal(" "))
                            .append(error.copy())
                            .append(Text.literal(" "))
                            .append(Text.literal(text).formatted(Formatting.WHITE)),
                    false);
        }
    }

    public static void sendErrorMessage(Text text) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(
                    prefix.copy()
                            .append(Text.literal(" "))
                            .append(error.copy())
                            .append(Text.literal(" "))
                            .append(text.copy().formatted(Formatting.WHITE)),
                    false);
        }
    }

    public static void sendModMessage(String text) {
        sendModMessage(Text.literal(text).formatted(Formatting.WHITE));
    }
}
