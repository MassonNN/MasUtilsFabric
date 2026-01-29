//package ru.massonnn.masutils.client.utils;
//
//import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
//import net.minecraft.text.Text;
//import ru.massonnn.masutils.client.waypoints.Waypoint;
//
///**
// * Utility class for rendering waypoints and related ESP elements.
// * Now acts as a bridge to the modernized RenderHelper.
// */
//public class RenderUtils {
//
//    /**
//     * Renders just the text name of a waypoint above its position.
//     */
//    public static void renderText(WorldRenderContext context, Waypoint waypoint) {
//        _RenderHelper.drawText(context, Text.of(waypoint.getName()),
//                waypoint.getPosition().toCenterPos().add(0, 1.0, 0), 1.0f, 0xFFFFFFFF, true, true);
//    }
//
//    /**
//     * Draws an ESP box around a waypoint and its name above it.
//     */
//    public static void drawESPBox(WorldRenderContext context, Waypoint waypoint) {
//        // Draw the wireframe box through walls
//        _RenderHelper.drawBox(context, waypoint.getPosition(), waypoint.getColor(), true, false, true);
//        renderText(context, waypoint);
//    }
//
//    /**
//     * Draws a line from the camera to the waypoint, along with an ESP box and name.
//     */
//    public static void drawEspCursorLine(WorldRenderContext context, Waypoint waypoint) {
//        // Draw the tracer line from camera to waypoint center
//        _RenderHelper.drawCursorLine(context, waypoint.getPosition().toCenterPos(), waypoint.getColor());
//        // Draw the box and text
//        drawESPBox(context, waypoint);
//    }
//}
