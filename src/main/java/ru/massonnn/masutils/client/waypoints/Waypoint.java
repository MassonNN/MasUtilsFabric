package ru.massonnn.masutils.client.waypoints;

import net.minecraft.util.math.BlockPos;

import java.awt.*;

public class Waypoint {
    private final double x, y, z;
    private final String name;
    private final Color color;
    private final WaypointType type;

    public Waypoint(double x, double y, double z, String name, Color color, WaypointType type) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.color = color;
        this.type = type;
    }

    public Waypoint(BlockPos position, String name, Color color, WaypointType type) {
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
        this.name = name;
        this.color = color;
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public WaypointType getType() { return this.type; }

    public BlockPos getPosition() {
        return new BlockPos((int) this.x, (int) this.y, (int) this.z);
    }

    public static Waypoint simpleTextWaypoint(BlockPos pos, String text) {
        return new Waypoint(pos, text, Color.WHITE, WaypointType.TEXT);
    }

    public static Waypoint espBox(BlockPos pos, String text) {
        return new Waypoint(pos, text, Color.WHITE, WaypointType.ESP);
    }
}
