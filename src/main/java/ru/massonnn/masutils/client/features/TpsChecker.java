package ru.massonnn.masutils.client.features;

import net.minecraft.client.MinecraftClient;

public class TpsChecker {
    private boolean requestedTps = false;
    private Long prevTime = null;
    private double lastTps = 20.0;
    private int tickCount = 0;

    public void requestTps() {
        requestedTps = true;
        prevTime = System.currentTimeMillis();
        tickCount = 0;
    }

    public void onTimeUpdate() {
        if (requestedTps) {
            tickCount++;
            if (prevTime != null && tickCount >= 20) {
                long time = System.currentTimeMillis() - prevTime;
                if (time > 0) {
                    double instantTps = Math.max(0, Math.min(20, 20000.0 / time));
                    lastTps = instantTps;
                    requestedTps = false;
                }
            }
        }
    }

    public double getLastTps() {
        return lastTps;
    }

    public boolean isRequested() {
        return requestedTps;
    }
}
