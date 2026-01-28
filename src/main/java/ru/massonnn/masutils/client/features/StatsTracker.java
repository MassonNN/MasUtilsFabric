package ru.massonnn.masutils.client.features;

import ru.massonnn.masutils.client.events.MineshaftEvent;
import ru.massonnn.masutils.client.hypixel.MineshaftType;

public class StatsTracker {
    private int blocksSinceLastMineshaft = 0;
    private int mineshaftsSinceFairy = 0;
    private int sessionMineshafts = 0;
    private boolean lastWasFairy = false;

    public StatsTracker() {
        MineshaftEvent.ON_ENTER_MINESHAFT.register(this::onEnterMineshaft);
        MineshaftEvent.ON_LEAVE_MINESHAFT.register(this::onLeaveMineshaft);
    }

    private void onEnterMineshaft(MineshaftType type) {
        sessionMineshafts++;
        blocksSinceLastMineshaft = 0;

        if (type == MineshaftType.FAIR1) {
            lastWasFairy = true;
            mineshaftsSinceFairy = 0;
        } else {
            if (lastWasFairy) {
                mineshaftsSinceFairy++;
            }
        }
    }

    private void onLeaveMineshaft() {
    }

    public void incrementBlocks() {
        blocksSinceLastMineshaft++;
    }

    public int getBlocksSinceLastMineshaft() {
        return blocksSinceLastMineshaft;
    }

    public int getMineshaftsSinceFairy() {
        return mineshaftsSinceFairy;
    }

    public int getSessionMineshafts() {
        return sessionMineshafts;
    }

    public void resetSession() {
        blocksSinceLastMineshaft = 0;
        mineshaftsSinceFairy = 0;
        sessionMineshafts = 0;
        lastWasFairy = false;
    }
}
