package ru.massonnn.masutils.client.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;

import java.time.Instant;
import java.util.*;

public class CommandExecutor {
    private static final LinkedList<String> commandQueue = new LinkedList<>();
    private static long lastCommandExecuted = 0;
    private static final int commandCooldown = 1;

    private static void enqueueCommand(String command) {
        if (MasUtilsConfigManager.get().dev.debug) {
            Masutils.LOGGER.info("Enqueue command: " + command);
        }
        commandQueue.add(command);
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (!commandQueue.isEmpty()) {
                long now = Instant.now().getEpochSecond();
                if (lastCommandExecuted + commandCooldown < now)  {
                    lastCommandExecuted = now;
                    assert client.player != null;
                    String command = commandQueue.pop();
                    client.player.networkHandler.sendChatCommand(command);
                } else {
                    Masutils.LOGGER.info("Waiting a second for command execution: " + commandQueue.peek());
                }
            }
        });
    }

    public static void executeCommand(String command) {
        enqueueCommand(command);
    }

    public static void sendPartyMessage(String message) {
        enqueueCommand("pc " + message);
    }
}
