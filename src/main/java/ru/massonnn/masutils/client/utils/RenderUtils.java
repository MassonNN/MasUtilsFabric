package ru.massonnn.masutils.client.utils;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.text.Text;
import ru.massonnn.masutils.client.waypoints.Waypoint;

public class RenderUtils {
    public static void renderText(WorldRenderContext context, Waypoint waypoint) {
        RenderHelper.renderText(context, Text.of(waypoint.getName()),
                waypoint.getPosition().toCenterPos().add(0, 1.0f, 0), true);
    }

    public static void drawESPBox(WorldRenderContext context, Waypoint waypoint) {
        final float[] colorComponents = ColorUtils.getFloatComponents(waypoint.getColor().getRGB());
        RenderHelper.renderFilled(context, waypoint.getPosition(), colorComponents,
                waypoint.getColor().getAlpha() / 255.0f, true);
        renderText(context, waypoint);
    }
}
