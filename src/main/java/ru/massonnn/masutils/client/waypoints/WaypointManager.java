package ru.massonnn.masutils.client.waypoints;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import ru.massonnn.masutils.client.utils.RenderUtils;

import java.util.ArrayList;
import java.util.List;

public class WaypointManager {
    private static final List<Waypoint> waypoints = new ArrayList<>();

    public static void applyHooks() {
        ServerWorldEvents.LOAD.register((server, t) -> WaypointManager.clearWaypoints());
        WorldRenderEvents.AFTER_TRANSLUCENT.register(WaypointManager::renderAllWaypoints);
    }

    public static void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
    }

    public static List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public static void clearWaypoints() {
        waypoints.clear();
    }

    public static void renderAllWaypoints(WorldRenderContext context) {
        for (Waypoint waypoint : waypoints) {
            switch (waypoint.getType()) {
                case TEXT -> RenderUtils.renderText(context, waypoint);
                case ESP -> RenderUtils.drawESPBox(context, waypoint);
                default -> RenderUtils.drawESPBox(context, waypoint);
            }
        }
    }
}
