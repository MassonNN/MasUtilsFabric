package ru.massonnn.masutils.client.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import ru.massonnn.masutils.client.hypixel.MineshaftType;

@Environment(EnvType.CLIENT)
public class MineshaftEvent {
    public static final Event<OnEnterMineshaft> ON_ENTER_MINESHAFT = EventFactory.createArrayBacked(
            OnEnterMineshaft.class,
            callbacks -> (type) -> {
                for (MineshaftEvent.OnEnterMineshaft callback : callbacks) {
                    callback.onEnterMineshaft(type);
                }
            });

    public static final Event<OnLeaveMineshaft> ON_LEAVE_MINESHAFT = EventFactory.createArrayBacked(
            OnLeaveMineshaft.class,
            callbacks -> () -> {
                for (MineshaftEvent.OnLeaveMineshaft callback : callbacks) {
                    callback.onLeaveMineshaft();
                }
            });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface OnEnterMineshaft {
        void onEnterMineshaft(MineshaftType type);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface OnLeaveMineshaft {
        void onLeaveMineshaft();
    }
}
