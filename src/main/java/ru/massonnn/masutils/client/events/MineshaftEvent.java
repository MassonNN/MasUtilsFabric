package ru.massonnn.masutils.client.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@Environment(EnvType.CLIENT)
public class MineshaftEvent {
    public static final Event<MineshaftJoined> MINESHAFT_JOINED_EVENT = EventFactory.createArrayBacked(
            MineshaftJoined.class,
            callbacks -> () -> {
                for (MineshaftEvent.MineshaftJoined callback : callbacks) {
                    callback.onMineshaftJoined();
                }
            }
    );

    public static final Event<MineshaftLeft> MINESHAFT_LEFT_EVENT = EventFactory.createArrayBacked(
            MineshaftLeft.class,
            callbacks -> () -> {
                for (MineshaftEvent.MineshaftLeft callback : callbacks) {
                    callback.onMineshaftLeft();
                }
            }
    );

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface MineshaftJoined {
        void onMineshaftJoined();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface MineshaftLeft {
        void onMineshaftLeft();
    }
}
