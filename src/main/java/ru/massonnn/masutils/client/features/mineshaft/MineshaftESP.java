package ru.massonnn.masutils.client.features.mineshaft;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import ru.massonnn.masutils.Masutils;
import ru.massonnn.masutils.client.config.MasUtilsConfigManager;
import ru.massonnn.masutils.client.events.MineshaftEvent;
import ru.massonnn.masutils.client.waypoints.Waypoint;
import ru.massonnn.masutils.client.waypoints.WaypointManager;
import ru.massonnn.masutils.client.waypoints.WaypointType;

import java.util.Objects;

public class MineshaftESP {
    static Waypoint mineshaftWaypoint;

    public static void init() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!MasUtilsConfigManager.get().mineshaft.mineshaftESP.createWaypointToMineshaft
                    || !MasUtilsConfigManager.get().mineshaft.mineshaftFeaturesToggle)
                return;
            client.world.getEntitiesByType(
                    EntityType.ARMOR_STAND,
                    client.player.getBoundingBox().expand(300),
                    (entity) -> {
                        if (entity.getName() != null) {
                            if (entity.getName().getString().contains(client.player.getName().getString())) {
                                return entity.getName().getString().contains("Mineshaft");
                            }
                        }
                        return false;
                    }).forEach(entity -> {
                        if (mineshaftWaypoint == null) {
                            mineshaftWaypoint = new Waypoint(
                                    entity.getBlockPos(),
                                    "Mineshaft",
                                    MasUtilsConfigManager.get().mineshaft.mineshaftESP.mineshaftESPColor,
                                    WaypointType.ESP_WITH_CURSOR_LINE,
                                    MasUtilsConfigManager.get().mineshaft.traceThickness);
                            WaypointManager.addWaypoint(mineshaftWaypoint);
                        }
                    });
        });

        MineshaftEvent.ON_LEAVE_MINESHAFT.register(() -> {
            mineshaftWaypoint = null;
        });
    }
}
