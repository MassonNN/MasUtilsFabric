package ru.massonnn.masutils.client.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class PingChecker {
    private long lastPing = 0;

    public void checkPing() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && player.networkHandler != null) {
            try {
                var entry = player.networkHandler.getPlayerListEntry(player.getUuid());
                if (entry != null) {
                    lastPing = entry.getLatency();
                }
            } catch (Exception e) {
                lastPing = 0;
            }
        }
    }

    public void onStatisticsReceived() {
    }

    public void onJoinGame() {
    }

    public long getLastPing() {
        return lastPing;
    }

    public boolean isRequested() {
        return false;
    }
}
