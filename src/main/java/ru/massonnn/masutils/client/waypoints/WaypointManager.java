package ru.massonnn.masutils.client.waypoints;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import ru.massonnn.masutils.client.events.WorldRenderExtractionCallback;
import ru.massonnn.masutils.client.utils.render.RenderHelper;
import ru.massonnn.masutils.client.utils.render.primitive.PrimitiveCollector;

import java.util.ArrayList;
import java.util.List;

public class WaypointManager {
    private static final List<Waypoint> waypoints = new ArrayList<>();

    public static void applyHooks() {
        ServerWorldEvents.LOAD.register((server, t) -> WaypointManager.clearWaypoints());
        WorldRenderExtractionCallback.EVENT.register(WaypointManager::extractRendering);
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

    public static void extractRendering(PrimitiveCollector collector) {
        waypoints.forEach(
                waypoint -> {
                    switch (waypoint.getType()) {
                        case WaypointType.ESP -> {
                            collector.submitOutlinedBox(
                                    new Box(waypoint.getPosition()),
                                    waypoint.getColor().getComponents(null),
                                    waypoint.getColor().getAlpha(),
                                    waypoint.getThickness(),
                                    true
                            );
                            collector.submitText(Text.literal(waypoint.getName()), waypoint.getPosition().toCenterPos().add(0d, 1.0d, 0d), true);
                        }
                        case WaypointType.ESP_WITH_CURSOR_LINE -> {
                            collector.submitOutlinedBox(
                                    new Box(waypoint.getPosition()),
                                    waypoint.getColor().getComponents(null),
                                    waypoint.getColor().getAlpha(),
                                    waypoint.getThickness(),
                                    true
                            );
                            collector.submitText(Text.literal(waypoint.getName()), waypoint.getPosition().toCenterPos().add(0d, 1.0d, 0d), true);
                            collector.submitCursorLine(
                                    waypoint.getPosition().toCenterPos(),
                                    waypoint.getColor().getComponents(null),
                                    waypoint.getColor().getAlpha(),
                                    waypoint.getThickness()
                            );
                        }
                        default -> {}
                    }

                }
        );
    }
}
