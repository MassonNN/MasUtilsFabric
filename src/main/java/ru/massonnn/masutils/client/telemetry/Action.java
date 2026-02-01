package ru.massonnn.masutils.client.telemetry;

import java.time.Instant;

public class Action {
    public long timestamp;
    public String context;
    public String target;

    public Action(String context, String target) {
        this.timestamp = Instant.now().getEpochSecond();
        this.context = context;
        this.target = target;
    }
}
