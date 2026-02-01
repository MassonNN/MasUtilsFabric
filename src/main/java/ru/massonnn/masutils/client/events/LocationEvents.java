package ru.massonnn.masutils.client.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import ru.massonnn.masutils.client.hypixel.Location;

@Environment(EnvType.CLIENT)
public class LocationEvents {
    public static final Event<SkyblockJoin> JOIN = EventFactory.createArrayBacked(SkyblockJoin.class,
            callbacks -> () -> {
                for (SkyblockJoin callback : callbacks) {
                    callback.onSkyblockJoin();
                }
            });

    public static final Event<SkyblockLeave> LEAVE = EventFactory.createArrayBacked(SkyblockLeave.class,
            callbacks -> () -> {
                for (SkyblockLeave callback : callbacks) {
                    callback.onSkyblockLeave();
                }
            });

    public static final Event<OnLocationChange> ON_LOCATION_CHANGE = EventFactory
            .createArrayBacked(OnLocationChange.class, callbacks -> (newLocation, prevLocation) -> {
                for (OnLocationChange callback : callbacks) {
                    callback.onLocationChange(newLocation, prevLocation);
                }
            });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockJoin {
        void onSkyblockJoin();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockLeave {
        void onSkyblockLeave();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface OnLocationChange {
        void onLocationChange(Location newLocation, Location prevLocation);
    }
}
