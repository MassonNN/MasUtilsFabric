package ru.massonnn.masutils.client.utils.render;

import org.joml.FrustumIntersection;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import ru.massonnn.masutils.mixins.accessors.FrustumInvoker;

public class FrustumUtils {
    public static Frustum getFrustum() {
        try {
            Object worldRenderer = MinecraftClient.getInstance().worldRenderer;
            Class<?> worldRendererClass = worldRenderer.getClass();
            java.lang.reflect.Field frustumField = null;
            
            String[] possibleFieldNames = {"frustum", "capturedFrustum", "field_41030", "frustum_1"};
            
            for (String fieldName : possibleFieldNames) {
                try {
                    frustumField = worldRendererClass.getDeclaredField(fieldName);
                    frustumField.setAccessible(true);
                    Object value = frustumField.get(worldRenderer);
                    if (value != null && (value instanceof Frustum || value.getClass().getName().equals("net.minecraft.client.render.Frustum"))) {
                        return (Frustum) value;
                    }
                } catch (NoSuchFieldException e) {
                    continue;
                }
            }
            
            for (java.lang.reflect.Field field : worldRendererClass.getDeclaredFields()) {
                String typeName = field.getType().getName();
                if (typeName.equals("net.minecraft.client.render.Frustum") || 
                    Frustum.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object value = field.get(worldRenderer);
                    if (value != null) {
                        return (Frustum) value;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isVisible(Box box) {
        Frustum frustum = getFrustum();
        if (frustum == null) return true;
        return frustum.isVisible(box);
    }

    public static boolean isVisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        Frustum frustum = getFrustum();
        if (frustum == null) return true;
        int plane = ((FrustumInvoker) frustum).invokeIntersectAab(minX, minY, minZ, maxX, maxY, maxZ);
        return plane == FrustumIntersection.INSIDE || plane == FrustumIntersection.INTERSECT;
    }
}