package ru.massonnn.masutils.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.Objects;

public class CommandExecutor {
    public static void executeCommand(String command) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.networkHandler.sendChatCommand(command);
        }
    }

    public static void sendPartyMessage(String message) {
        executeCommand("pc " + message);
    }
}
