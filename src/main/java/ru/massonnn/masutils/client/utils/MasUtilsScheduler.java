package ru.massonnn.masutils.client.utils;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MasUtilsScheduler {
    private static final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public static void init() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            Runnable task;
            while ((task = tasks.poll()) != null) {
                task.run();
            }
        });
    }

    public static void schedule(Runnable runnable) {
        tasks.add(runnable);
    }
}
